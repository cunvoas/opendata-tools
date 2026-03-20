package com.github.cunvoas.geoserviceisochrone.service.solver.compute;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
 * @see IterativeComputationDeficitStrategy pour l'approche iterative pas-a-pas
 */
@Slf4j
public class LeastSquaresStrategy extends AbstractComputationtrategy {

    @Override
    public List<ParkProposal> compute(Map<String, ParkProposalWork> carreMap,
            Double minSquareMeterPerCapita,
            Double recoSquareMeterPerCapita,
            Integer urbanDistance) {
        List<ParkProposal> proposals = new ArrayList<>();
        if (carreMap == null || carreMap.isEmpty()) {
            log.warn("Carte vide, aucune proposition par moindres carres.");
            return proposals;
        }

        // Mesures: deficit par habitant pondere par la population
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
            log.info("Aucun deficit a reduire via la strategie moindres carres.");
            return proposals;
        }

        // Ajuste un modele constant (deficit moyen pondere) : minimise la somme des erreurs (chi2)
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(0);
        double[] coeffs = fitter.fit(observedPoints.toList());
        double additionPerCapita = Math.max(0d, coeffs[0]);
        additionPerCapita = Math.min(additionPerCapita, recoSquareMeterPerCapita);

        log.info("Strategie moindres carres: ajout moyen par habitant estime a {} m2", additionPerCapita);

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

            ParkProposal proposal = new ParkProposal();
            proposal.setAnnee(work.getAnnee());
            proposal.setIdInspire(work.getIdInspire());
            proposal.setParkSurface(BigDecimal.valueOf(newParkSurface));
            proposal.setCentre(work.getCentre());
            proposal.setIsDense(work.getIsDense());
            proposals.add(proposal);

            // Applique la proposition localement
            work.setNewSurface(BigDecimal.valueOf(newParkSurface));
            BigDecimal baseMissing = work.getNewMissingSurface() != null ? work.getNewMissingSurface() : work.getMissingSurface();
            if (baseMissing == null) {
                baseMissing = BigDecimal.ZERO;
            }
            work.setNewMissingSurface(baseMissing.subtract(BigDecimal.valueOf(newParkSurface)).max(BigDecimal.ZERO));

            double newTotalSurface = (work.getAccessingSurface() != null ? work.getAccessingSurface().doubleValue() : 0d) + newParkSurface;
            double newSurfacePerCapita = population > 0 ? newTotalSurface / population : 0d;
            work.setNewSurfacePerCapita(population > 0 ? BigDecimal.valueOf(newSurfacePerCapita) : null);
            work.setSurfacePerCapita(population > 0 ? BigDecimal.valueOf(newSurfacePerCapita) : work.getSurfacePerCapita());

            // Propagation aux voisins dans le rayon d'accessibilite
            for (ParkProposalWork neighbor : findNeighbors(work.getIdInspire(), carreMap, urbanDistance)) {
                double neighborPopulation = neighbor.getAccessingPopulation() != null ? neighbor.getAccessingPopulation().doubleValue() : 0d;
                double neighborTotalSurface = (neighbor.getAccessingSurface() != null ? neighbor.getAccessingSurface().doubleValue() : 0d) + newParkSurface;
                if (neighborPopulation > 0) {
                    neighbor.setNewSurfacePerCapita(BigDecimal.valueOf(neighborTotalSurface / neighborPopulation));
                } else {
                    neighbor.setNewSurfacePerCapita(null);
                }
                neighbor.setNewSurface(BigDecimal.valueOf(neighborTotalSurface));
                BigDecimal neighborBaseMissing = neighbor.getNewMissingSurface() != null ? neighbor.getNewMissingSurface() : neighbor.getMissingSurface();
                if (neighborBaseMissing == null) {
                    neighborBaseMissing = BigDecimal.ZERO;
                }
                neighbor.setNewMissingSurface(neighborBaseMissing.subtract(BigDecimal.valueOf(newParkSurface)).max(BigDecimal.ZERO));
            }
        }

        return proposals;
    }

}