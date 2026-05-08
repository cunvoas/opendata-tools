package com.github.cunvoas.geoserviceisochrone.service.solver.compute;


import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposal;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;

import lombok.extern.slf4j.Slf4j;

/**
 * Strategie d'estimation globale par moindres carres (chi2) sur les deficits.
 * <p>Objectif: estimer une addition moyenne de surface de parc par habitant
 * qui reduit au mieux, au sens des moindres carres ponderes, les deficits
 * observes dans les carres les plus en manque.</p>
 *
 * <p><strong>Principe statistique :</strong></p>
 * <ul>
 *   <li>Chaque carre fournit une observation: deficit_par_hab = max(0, reco - densite)</li>
 *   <li>Le poids de l'observation est la population ayant acces au carre (chi2)</li>
 *   <li>Un ajusteur de degree 0 (modele constant) renvoie le deficit moyen pondere</li>
 *   <li>Le resultat est borne par la recommandation pour eviter tout depassement</li>
 * </ul>
 *
 * <p><strong>Algorithme :</strong></p>
 * <ol>
 *   <li>Filtrer les carres avec population > 0 et densite <= seuil minimal</li>
 *   <li>Construire les observations (population, deficit_par_hab)</li>
 *   <li>Ajuster un modele constant via {@link PolynomialCurveFitter#create(int)} avec degree 0</li>
 *   <li>En deduire additionParHab = min(max(coeff0, 0), recoSquareMeterPerCapita)</li>
 *   <li>Pour chaque carre deficit, proposer surface = additionParHab * population (borne a 40 000 m2)</li>
 *   <li>Ignorer les propositions < 1000 m2 (MIN_PARK_SURFACE)</li>
 *   <li>Mettre a jour newSurface / newSurfacePerCapita / newMissingSurface pour le carre</li>
 *   <li>Propager l'impact aux voisins dans le rayon urbanDistance (accessibilite)</li>
 * </ol>
 *
 * <p><strong>Effets de bord :</strong></p>
 * <ul>
 *   <li>Modifie in-place les instances {@link ParkProposalWork} (newSurface*, surfacePerCapita)</li>
 *   <li>Retourne la liste des {@link ParkProposal} retenues (>= 1000 m2)</li>
 *   <li>Ne boucle pas iterativement: applique une estimation globale, puis mise a jour directe</li>
 * </ul>
 *
 * @see IterativeComputationDeficit1Strategy pour l'approche iterative pas-a-pas
 */
@Slf4j
public class LeastSquaresNeigbour1Strategy extends AbstractComputationtrategy {

    @Override
    public List<ParkProposal> compute(Map<String, ParkProposalWork> carreMap,
            Double minSquareMeterPerCapita,
            Double recoSquareMeterPerCapita,
            Integer urbanDistance) {
        /*
         * Etapes clefs :
         * 1) Construire les observations deficit_par_hab pour les carres en manque (poids = population).
         * 2) Ajuster un modele constant pour estimer une addition moyenne par habitant (borne par reco).
         * 3) Evaluer chaque carre deficit et ses voisins OMS : calculer chi2 avant/apres en ajoutant la surface partagee.
         * 4) Choisir la proposition qui minimise le chi2 global, appliquer la surface au carre central et propager aux voisins.
         * 5) Tracer chaque iteration dans un CSV et retourner les ParkProposal retenues (>= 1000 m2).
         */

        List<ParkProposal> proposals = new ArrayList<>();
        if (carreMap == null || carreMap.isEmpty()) {
            log.warn("Carte vide, aucune proposition par moindres carres.");
            return proposals;
        }

        List<String> csvLines = new ArrayList<>();
        csvLines.add("iteration,carre_centre,carre_impacte,role,population,surf_per_capita_avant,surf_per_capita_apres,missing_avant,missing_apres,deficit_avant,deficit_apres,ajout_surface");

        // Boucle jusqu'a couvrir la population (ou aucun gain)
        for (int step = 0; step < carreMap.size(); step++) {
            WeightedObservedPoints observedPoints = new WeightedObservedPoints();
            Map<String, Double> deficitPerCapitaById = new HashMap<>();
            carreMap.values().forEach(work -> {
                double population = work.getAccessingPopulation() != null ? work.getAccessingPopulation().doubleValue() : 0d;
                double surfacePerCapita = work.getSurfacePerCapita() != null ? work.getSurfacePerCapita().doubleValue() : 0d;
                if (population <= 0) {
                    return;
                }
                double deficitPerCapita = Math.max(0d, recoSquareMeterPerCapita - surfacePerCapita);
                if (deficitPerCapita > 0 && surfacePerCapita <= minSquareMeterPerCapita) {
                    observedPoints.add(population, 0d, deficitPerCapita);
                    deficitPerCapitaById.put(work.getIdInspire(), deficitPerCapita);
                }
            });

            if (observedPoints.toList().isEmpty()) {
                log.info("Aucun deficit restant a reduire via la strategie moindres carres (iteration {}).", step);
                break;
            }

            PolynomialCurveFitter fitter = PolynomialCurveFitter.create(0);
            double[] coeffs = fitter.fit(observedPoints.toList());
            double additionPerCapita = Math.max(0d, coeffs[0]);
            additionPerCapita = Math.min(additionPerCapita, recoSquareMeterPerCapita);

            double baselineChi2 = 0d;
            for (ParkProposalWork work : carreMap.values()) {
                baselineChi2 += chi2(work, recoSquareMeterPerCapita);
            }

            double bestScore = baselineChi2;
            String bestId = null;
            double bestNewParkSurface = 0d;
            List<ParkProposalWork> bestNeighbors = new ArrayList<>();

            // Recherche du carre dont l'ajout (propagant aux voisins OMS) minimise le chi2 global
            for (Map.Entry<String, Double> entry : deficitPerCapitaById.entrySet()) {
                ParkProposalWork work = carreMap.get(entry.getKey());
                double population = work.getAccessingPopulation() != null ? work.getAccessingPopulation().doubleValue() : 0d;
                if (population <= 0) {
                    continue;
                }

                double deficitPerCapita = entry.getValue();
                double appliedPerCapita = Math.min(deficitPerCapita, additionPerCapita);
                double newParkSurface = Math.min(appliedPerCapita * population, CARRE_SURFACE);
                if (newParkSurface < MIN_PARK_SURFACE) {
                    continue;
                }

                Map<String, ParkProposalWork> impacted = new LinkedHashMap<>();
                impacted.put(work.getIdInspire(), work);
                for (ParkProposalWork neighbor : findNeighbors(work.getIdInspire(), carreMap, urbanDistance)) {
                    impacted.put(neighbor.getIdInspire(), neighbor);
                }

                double impactedBaseline = 0d;
                double impactedAfter = 0d;
                for (ParkProposalWork impactedWork : impacted.values()) {
                    double impactedPop = impactedWork.getAccessingPopulation() != null ? impactedWork.getAccessingPopulation().doubleValue() : 0d;
                    if (impactedPop <= 0) {
                        continue;
                    }
                    impactedBaseline += chi2(impactedWork, recoSquareMeterPerCapita);

                    double baseSurface = impactedWork.getNewSurface() != null
                            ? impactedWork.getNewSurface().doubleValue()
                            : impactedWork.getAccessingSurface() != null ? impactedWork.getAccessingSurface().doubleValue() : 0d;
                    double totalSurface = baseSurface + newParkSurface;
                    double newSurfacePerCapita = impactedPop > 0 ? totalSurface / impactedPop : 0d;
                    double newDeficit = Math.max(0d, recoSquareMeterPerCapita - newSurfacePerCapita);
                    impactedAfter += impactedPop * newDeficit * newDeficit;
                }

                double totalAfter = baselineChi2 - impactedBaseline + impactedAfter;
                if (totalAfter < bestScore) {
                    bestScore = totalAfter;
                    bestId = work.getIdInspire();
                    bestNewParkSurface = newParkSurface;
                    bestNeighbors = new ArrayList<>(impacted.values());
                }
            }

            if (bestId == null) {
                log.info("Iteration {}: aucune proposition retenue apres evaluation des voisins OMS.", step);
                break;
            }

            // Enregistre l'impact avant mise a jour
            for (ParkProposalWork impacted : bestNeighbors) {
                double pop = impacted.getAccessingPopulation() != null ? impacted.getAccessingPopulation().doubleValue() : 0d;
                double beforeSurface = impacted.getNewSurface() != null ? impacted.getNewSurface().doubleValue()
                        : impacted.getAccessingSurface() != null ? impacted.getAccessingSurface().doubleValue() : 0d;
                double beforeSurfacePerCapita = pop > 0 ? beforeSurface / pop : 0d;
                double beforeMissing = impacted.getNewMissingSurface() != null ? impacted.getNewMissingSurface().doubleValue()
                        : impacted.getMissingSurface() != null ? impacted.getMissingSurface().doubleValue() : 0d;
                double afterTotalSurface = beforeSurface + bestNewParkSurface;
                double afterSurfacePerCapita = pop > 0 ? afterTotalSurface / pop : 0d;
                double afterMissing = Math.max(0d, beforeMissing - bestNewParkSurface);
                double deficitAvant = Math.max(0d, recoSquareMeterPerCapita - beforeSurfacePerCapita);
                double deficitApres = Math.max(0d, recoSquareMeterPerCapita - afterSurfacePerCapita);
                String role = impacted.getIdInspire().equals(bestId) ? "central" : "neighbor";
                csvLines.add(String.format(Locale.ROOT,
                        "%d,%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f",
                        step, bestId, impacted.getIdInspire(), role, pop, beforeSurfacePerCapita,
                        afterSurfacePerCapita, beforeMissing, afterMissing, deficitAvant, deficitApres, bestNewParkSurface));
            }

            ParkProposalWork bestWork = carreMap.get(bestId);
            ParkProposal proposal = new ParkProposal();
            proposal.setAnnee(bestWork.getAnnee());
            proposal.setIdInspire(bestWork.getIdInspire());
            proposal.setParkSurface(BigDecimal.valueOf(bestNewParkSurface));
            proposal.setCentre(bestWork.getCentre());
            proposal.setIsDense(bestWork.getIsDense());
            proposals.add(proposal);

            // Applique la proposition sur le carre choisi
            bestWork.setNewSurface(BigDecimal.valueOf(bestNewParkSurface));
            BigDecimal baseMissing = bestWork.getNewMissingSurface() != null ? bestWork.getNewMissingSurface() : bestWork.getMissingSurface();
            if (baseMissing == null) {
                baseMissing = BigDecimal.ZERO;
            }
            bestWork.setNewMissingSurface(baseMissing.subtract(BigDecimal.valueOf(bestNewParkSurface)).max(BigDecimal.ZERO));

            double bestPopulation = bestWork.getAccessingPopulation() != null ? bestWork.getAccessingPopulation().doubleValue() : 0d;
            double bestBaseSurface = bestWork.getNewSurface() != null ? bestWork.getNewSurface().doubleValue()
                    : bestWork.getAccessingSurface() != null ? bestWork.getAccessingSurface().doubleValue() : 0d;
            double bestTotalSurface = bestBaseSurface + bestNewParkSurface;
            double bestSurfacePerCapita = bestPopulation > 0 ? bestTotalSurface / bestPopulation : 0d;
            bestWork.setNewSurfacePerCapita(bestPopulation > 0 ? BigDecimal.valueOf(bestSurfacePerCapita) : null);
            bestWork.setSurfacePerCapita(bestPopulation > 0 ? BigDecimal.valueOf(bestSurfacePerCapita) : bestWork.getSurfacePerCapita());

            // Propagation aux voisins (dont le carre central est deja present dans la liste)
            for (ParkProposalWork neighbor : bestNeighbors) {
                if (neighbor.getIdInspire().equals(bestId)) {
                    continue;
                }
                double neighborPopulation = neighbor.getAccessingPopulation() != null ? neighbor.getAccessingPopulation().doubleValue() : 0d;
                double neighborBaseSurface = neighbor.getNewSurface() != null ? neighbor.getNewSurface().doubleValue()
                        : neighbor.getAccessingSurface() != null ? neighbor.getAccessingSurface().doubleValue() : 0d;
                double neighborTotalSurface = neighborBaseSurface + bestNewParkSurface;
                if (neighborPopulation > 0) {
                    neighbor.setNewSurfacePerCapita(BigDecimal.valueOf(neighborTotalSurface / neighborPopulation));
                    neighbor.setSurfacePerCapita(BigDecimal.valueOf(neighborTotalSurface / neighborPopulation));
                } else {
                    neighbor.setNewSurfacePerCapita(null);
                }
                neighbor.setNewSurface(BigDecimal.valueOf(neighborTotalSurface));
                BigDecimal neighborBaseMissing = neighbor.getNewMissingSurface() != null ? neighbor.getNewMissingSurface() : neighbor.getMissingSurface();
                if (neighborBaseMissing == null) {
                    neighborBaseMissing = BigDecimal.ZERO;
                }
                neighbor.setNewMissingSurface(neighborBaseMissing.subtract(BigDecimal.valueOf(bestNewParkSurface)).max(BigDecimal.ZERO));
            }

            log.info("Strategie moindres carres (voisins OMS) : iteration {}, carre {} retenu avec ajout de {} m2, score {}",
                    step, bestId, bestNewParkSurface, bestScore);
        }

        if (csvLines.size() > 1) {
            writeCsvImpact(Paths.get("target", "least-squares-neighbor-impacts.csv"), csvLines);
        }

        return proposals;
    }

    /**
     * Calcule la contribution chi2 (moindres carres ponderee) d'un carre :
     * population * deficit^2. Le carre du deficit garantit une penalisation
     * quadratique des manques (plus severement pour les gros deficits) et une
     * valeur toujours positive pour la somme.
     */
    private double chi2(ParkProposalWork work, double recoSquareMeterPerCapita) {
        double population = work.getAccessingPopulation() != null ? work.getAccessingPopulation().doubleValue() : 0d;
        if (population <= 0) {
            return 0d;
        }
        double surfacePerCapita = work.getSurfacePerCapita() != null ? work.getSurfacePerCapita().doubleValue() : 0d;
        double deficit = Math.max(0d, recoSquareMeterPerCapita - surfacePerCapita);
        // Penalise quadratiquement le manque (chi2) et le pondere par la population exposee
        return population * deficit * deficit;
    }

    /**
     * Ecrit un CSV de trace listant, a chaque iteration, l'impact du carre choisi
     * sur lui-meme et ses voisins OMS: surf per capita avant/apres, manque avant/apres,
     * deficit avant/apres et surface ajoutee. Utile pour debug/audit du solveur.
     */
    private void writeCsvImpact(Path output, List<String> lines) {
        try {
            if (output.getParent() != null) {
                Files.createDirectories(output.getParent());
            }
            Files.write(output, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Bilan voisins OMS écrit dans {} ({} lignes)", output.toAbsolutePath(), lines.size() - 1);
        } catch (Exception e) {
            log.error("Impossible d'ecrire le CSV des impacts voisins OMS", e);
        }
    }

}