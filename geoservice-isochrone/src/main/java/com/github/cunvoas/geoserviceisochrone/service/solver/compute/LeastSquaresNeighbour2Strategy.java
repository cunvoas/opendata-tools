package com.github.cunvoas.geoserviceisochrone.service.solver.compute;


import java.math.BigDecimal;
import java.util.ArrayList;
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
 * @see IterativeComputationDeficit1Strategy pour l'approche iterative pas-a-pas
 */
@Slf4j
public class LeastSquaresNeighbour2Strategy extends AbstractComputationtrategy {

    /**
     * Calcule les propositions de parcs de façon iterative, carreau par carreau.
     *
     * <p><strong>Principe :</strong> A chaque iteration :</p>
     * <ol>
     *   <li>Construire les observations chi2 sur les carreaux encore deficitaires
     *       (population > 0 et surface/hab &lt;= minSquareMeterPerCapita) :
     *       poids = population, valeur = deficit_par_hab = max(0, reco - surface/hab).</li>
     *   <li>Ajuster un modele constant (degre 0) par moindres carres pour estimer
     *       l'addition moyenne par habitant ({@code additionPerCapita}), bornee entre 0 et reco.</li>
     *   <li>Selectionner le carreau avec le plus grand deficit (tri decroissant).</li>
     *   <li>Si son deficit est inferieur ou egal au seuil minimum, arreter : tous les carreaux sont traites.</li>
     *   <li>Calculer la surface a ajouter = min(deficit_carreau, additionPerCapita) * population,
     *       bornee par CARRE_SURFACE. Si &lt; MIN_PARK_SURFACE, passer au suivant.</li>
     *   <li>Appliquer la proposition sur le carreau central (newSurface, surfacePerCapita, newMissingSurface).</li>
     *   <li>Propager la surface ajoutee aux voisins dans le rayon urbanDistance
     *       (mise a jour de newSurface, surfacePerCapita, newMissingSurface pour chaque voisin).</li>
     *   <li>Recommencer jusqu'a ce qu'il n'y ait plus de deficit ou que tous les carreaux soient traites.</li>
     * </ol>
     *
     * @param carreMap               carte des carreaux indexee par idInspire (modifiee in-place)
     * @param minSquareMeterPerCapita seuil en dessous duquel un carreau est considere deficitaire (m²/hab)
     * @param recoSquareMeterPerCapita objectif de surface par habitant a atteindre (m²/hab, ex: 12 m²/hab OMS)
     * @param urbanDistance           rayon d'accessibilite pour la propagation aux voisins (metres)
     * @return liste des {@link ParkProposal} retenues (&gt;= MIN_PARK_SURFACE m²)
     */
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

        // Boucle iterative : le pas chi2 est inferieur au deficit reel (addition partielle par iteration),
        // il faut donc beaucoup plus d'iterations que carreMap.size() pour converger.
        // Le facteur 10 offre une marge suffisante sans risquer une boucle infinie.
        int maxIterations = carreMap.size() * 100;
        for (int step = 0; step < maxIterations; step++) {

            // --- Etape 1 : Construire les observations chi2 sur les carreaux deficitaires ---
            WeightedObservedPoints observedPoints = new WeightedObservedPoints();
            carreMap.values().forEach(work -> {
                double population = work.getAccessingPopulation() != null ? work.getAccessingPopulation().doubleValue() : 0d;
                double surfacePerCapita = work.getSurfacePerCapita() != null ? work.getSurfacePerCapita().doubleValue() : 0d;
                if (population <= 0) {
                    return;
                }
                double deficitPerCapita = Math.max(0d, recoSquareMeterPerCapita - surfacePerCapita);
                // N'inclure que les carreaux vraiment deficitaires (sous le seuil minimum)
                if (deficitPerCapita > 0 && surfacePerCapita <= minSquareMeterPerCapita) {
                    observedPoints.add(population, 0d, deficitPerCapita);
                }
            });

            if (observedPoints.toList().isEmpty()) {
                log.info("Iteration {} : aucun deficit restant, arret.", step);
                break;
            }

            // --- Etape 2 : Fit chi2 (modele constant = deficit moyen pondere par la population) ---
            PolynomialCurveFitter fitter = PolynomialCurveFitter.create(0);
            double[] coeffs = fitter.fit(observedPoints.toList());
            double additionPerCapita = Math.max(0d, coeffs[0]);
            // Borner pour ne pas depasser la recommandation
            additionPerCapita = Math.min(additionPerCapita, recoSquareMeterPerCapita);
            log.info("Iteration {} : addition estimee par chi2 = {} m2/hab", step, additionPerCapita);

            // --- Etape 3 : Selectionner le carreau avec le plus grand deficit ---
            List<ParkProposalWork> sorted = sortProposalsByDeficit(carreMap);
            if (sorted.isEmpty()) {
                break;
            }
            ParkProposalWork toProcess = sorted.get(0);

            // --- Etape 4 : Verifier que le carreau est encore deficitaire ---
            double surfacePerCapita = toProcess.getSurfacePerCapita() != null ? toProcess.getSurfacePerCapita().doubleValue() : 0d;
            if (surfacePerCapita > minSquareMeterPerCapita) {
                log.info("Iteration {} : tous les carreaux sont traites (meilleur = {} m2/hab).", step, surfacePerCapita);
                break;
            }

            double population = toProcess.getAccessingPopulation() != null ? toProcess.getAccessingPopulation().doubleValue() : 0d;
            if (population <= 0) {
                log.info("Iteration {} : carreau {} sans population, arret.", step, toProcess.getIdInspire());
                break;
            }

            // --- Etape 5 : Calculer la surface a ajouter ---
            // On applique l'addition estimee par chi2, plafonnee au deficit reel du carreau
            double deficitPerCapita = Math.max(0d, recoSquareMeterPerCapita - surfacePerCapita);
            double appliedPerCapita = Math.min(deficitPerCapita, additionPerCapita);
            double newParkSurface = Math.min(appliedPerCapita * population, CARRE_SURFACE);

            if (newParkSurface < MIN_PARK_SURFACE) {
                log.info("Iteration {} : surface proposee {} m2 < MIN_PARK_SURFACE pour le carreau {}, arret.",
                        step, newParkSurface, toProcess.getIdInspire());
                break;
            }

            // --- Etape 6 : Appliquer la proposition sur le carreau central ---
            ParkProposal proposal = new ParkProposal();
            proposal.setAnnee(toProcess.getAnnee());
            proposal.setIdInspire(toProcess.getIdInspire());
            proposal.setParkSurface(BigDecimal.valueOf(newParkSurface));
            proposal.setCentre(toProcess.getCentre());
            proposal.setIsDense(toProcess.getIsDense());
            proposals.add(proposal);

            // newSurface = surface du nouveau parc propose (remplace, pas cumul)
            toProcess.setNewSurface(BigDecimal.valueOf(newParkSurface));
            // newMissingSurface diminue du parc ajoute (pas de max(0) : conforme a la reference)
            toProcess.setNewMissingSurface(toProcess.getNewMissingSurface().subtract(BigDecimal.valueOf(newParkSurface)));

            // Recalcul surface/hab depuis la base initiale accessingSurface (les attributs sans New ne changent pas)
            double newTotalSurface = toProcess.getAccessingSurface().doubleValue() + newParkSurface;
            double newSurfacePerCapita = newTotalSurface / population;
            // setSurfacePerCapita mis a jour uniquement pour piloter le tri a l'iteration suivante
            toProcess.setSurfacePerCapita(BigDecimal.valueOf(newSurfacePerCapita));

            // --- Etape 7 : Propager la surface ajoutee aux voisins dans le rayon d'accessibilite ---
            // La nouvelle surface est positionnee sur le centre et recalculee vis-a-vis de la population de chaque voisin
            List<ParkProposalWork> neighbors = findNeighbors(toProcess.getIdInspire(), carreMap, urbanDistance);
            for (ParkProposalWork neighbor : neighbors) {
                // Base initiale du voisin : accessingSurface (jamais modifie)
                double neighborTotalSurface = neighbor.getAccessingSurface().doubleValue() + newParkSurface;

                neighbor.setNewSurface(new BigDecimal(String.valueOf(neighborTotalSurface)));

                double neighborPopulation = neighbor.getAccessingPopulation().doubleValue();
                if (neighborPopulation != 0) {
                    // surface/hab recalculee vis-a-vis de la population du voisin
                    double neighborSurfacePerCapita = neighborTotalSurface / neighborPopulation;
                    neighbor.setNewSurfacePerCapita(BigDecimal.valueOf(neighborSurfacePerCapita));
                } else {
                    neighbor.setNewSurfacePerCapita(null);
                }

                // newMissingSurface du voisin base sur le missing du carreau CENTRAL (apres soustraction)
                neighbor.setNewMissingSurface(
                    toProcess.getNewMissingSurface().subtract(BigDecimal.valueOf(newParkSurface)).max(BigDecimal.ZERO)
                );
                // setSurfacePerCapita du voisin non modifie : seul le carreau central pilote le tri
            }

            log.info("Iteration {} : carreau {} traite, ajout {} m2, {} voisins mis a jour.",
                    step, toProcess.getIdInspire(), newParkSurface, neighbors.size());
        }

        return proposals;
    }

}