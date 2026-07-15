package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposal;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;
import com.github.cunvoas.geoserviceisochrone.service.solver.helper.HaversineDistance;
import com.github.cunvoas.geoserviceisochrone.service.solver.helper.ParkProposalClusterable;

import lombok.extern.slf4j.Slf4j;

/**
 * Strategie de calcul iterative utilisant DBSCAN pour la selection des candidats.
 * <p>
 * Contrairement a {@link IterativeComputationDeficit2Strategy} qui selectionne
 * le carreau le plus deficitaire globalement, cette variante re-clusterise
 * a chaque iteration les seuls carreaux encore deficitaires avec DBSCAN :</p>
 * <ol>
 *   <li>Recupere les carreaux sous le seuil {@code minSquareMeterPerCapita}</li>
 *   <li>Les clusterise avec DBSCAN (eps = {@code urbanDistance}, minPts = 3)</li>
 *   <li>Selectionne le cluster au deficit total le plus eleve</li>
 *   <li>Prend le carreau le plus deficitaire de ce cluster</li>
 *   <li>Si aucun cluster formable, fallback sur le tri global</li>
 * </ol>
 * <p>
 * L'objectif est de concentrer les propositions dans les zones spatialement
 * denses en deficits, en s'adaptant a l'etat courant du territoire.
 * </p>
 * 
 * @see DBSCANClusterer
 * @see HaversineDistance
 * @see ParkProposalClusterable
 */
@Slf4j
public class IterativeComputationDBSCANStrategy extends AbstractComputationtrategy {

    private final double minParkSurface;
    private static final int DBSCAN_MIN_PTS = 3;

    public IterativeComputationDBSCANStrategy(double minParkSurface) {
        this.minParkSurface = minParkSurface;
    }

    @Override
    public List<ParkProposal> compute(Map<String, ParkProposalWork> carreMap,
                                      Double minSquareMeterPerCapita,
                                      Double recoSquareMeterPerCapita,
                                      Integer urbanDistance) {
        List<ParkProposal> proposals = new ArrayList<>();
        for (int i = 0; i < carreMap.size(); i++) {
            ParkProposal proposal = calculeEtapeProposition(
                    minParkSurface, carreMap,
                    minSquareMeterPerCapita, recoSquareMeterPerCapita, urbanDistance);
            if (proposal != null) {
                proposals.add(proposal);
            }
        }
        return proposals;
    }

    /**
     * Calcule une etape de proposition en clusterisant dynamiquement les
     * carreaux deficitaires avec DBSCAN.
     * <p>
     * A chaque iteration :</p>
     * <ol>
     *   <li>Filtre les carreaux encore sous {@code minSquareMeterPerCapita}</li>
     *   <li>Les clusterise avec DBSCAN (eps = {@code urbanDistance}, minPts = 3)</li>
     *   <li>Classe les clusters par deficit total decroissant</li>
     *   <li>Selectionne le carreau le plus deficitaire du meilleur cluster</li>
     *   <li>Si aucun cluster formable (tous bruit), fallback sur le tri global</li>
     * </ol>
     * 
     * @param minParkSurface surface minimale pour creer un parc (m²)
     * @param squaresOnTerritoryMap carte des carreaux modifiee par la proposition
     * @param minSquareMeterPerCapita seuil minimal de surface par habitant (m²/hab)
     * @param recoSquareMeterPerCapita densite recommandee (m²/hab)
     * @param urbanDistance distance d'accessibilite (m)
     * @return proposition de parc, ou null si aucun carreau eligible
     */
    public ParkProposal calculeEtapeProposition(Double minParkSurface,
                                                 Map<String, ParkProposalWork> squaresOnTerritoryMap,
                                                 Double minSquareMeterPerCapita,
                                                 Double recoSquareMeterPerCapita,
                                                 Integer urbanDistance) {
        ParkProposal proposalResult = null;

        // Étape 1 : filtre les carreaux encore déficitaires
        List<ParkProposalWork> deficitaires = squaresOnTerritoryMap.values().stream()
                .filter(w -> w.getNewSurfacePerCapita() != null
                        && w.getNewSurfacePerCapita().doubleValue() <= minSquareMeterPerCapita)
                .collect(Collectors.toList());

        if (deficitaires.isEmpty()) {
            log.info("Toutes les propositions de la commune sont traitees.");
            return proposalResult;
        }

        ParkProposalWork toProcess = null;

        // Étape 2 : clusterise les déficitaires avec DBSCAN
        double epsMeters = urbanDistance;
        List<ParkProposalClusterable> points = deficitaires.stream()
                .map(ParkProposalClusterable::new)
                .collect(Collectors.toList());

        DBSCANClusterer<ParkProposalClusterable> dbscan =
                new DBSCANClusterer<>(epsMeters, DBSCAN_MIN_PTS, new HaversineDistance());
        List<Cluster<ParkProposalClusterable>> clusters = dbscan.cluster(points);

        if (clusters.size() >= 1) {
            // Étape 3 : classe les clusters par déficit total décroissant
            List<Cluster<ParkProposalClusterable>> rankedClusters = clusters.stream()
                    .sorted((c1, c2) -> {
                        double total1 = c1.getPoints().stream()
                                .mapToDouble(p -> p.getWork().getNewMissingSurface().doubleValue())
                                .sum();
                        double total2 = c2.getPoints().stream()
                                .mapToDouble(p -> p.getWork().getNewMissingSurface().doubleValue())
                                .sum();
                        return Double.compare(total2, total1);
                    })
                    .collect(Collectors.toList());

            // Étape 4 : prend le plus déficitaire du meilleur cluster
            toProcess = rankedClusters.get(0).getPoints().stream()
                    .sorted((a, b) -> Double.compare(
                            b.getWork().getNewMissingSurface().doubleValue(),
                            a.getWork().getNewMissingSurface().doubleValue()))
                    .findFirst()
                    .map(ParkProposalClusterable::getWork)
                    .orElse(null);
        }

        // Étape 5 : fallback — pas de cluster formable (tous bruit)
        if (toProcess == null) {
            toProcess = deficitaires.stream()
                    .sorted(Comparator.comparing(
                            ParkProposalWork::getNewMissingSurface,
                            Comparator.nullsLast(Comparator.reverseOrder())))
                    .findFirst()
                    .orElse(null);
        }

        if (toProcess == null) {
            return proposalResult;
        }

        // Étape 6 : calcule la surface de parc nécessaire pour atteindre
        // la densité recommandée, plafonnée à CARRE_SURFACE (40 000 m²)
        double deficitPerCapita = Math.max(0d, recoSquareMeterPerCapita - toProcess.getNewSurfacePerCapita().doubleValue());
        double proposedParkSurface = Math.min(
                deficitPerCapita * toProcess.getAccessingPopulation().doubleValue(),
                AbstractComputationtrategy.CARRE_SURFACE);

        // Étape 7 : seuil de validation — ≥ minParkSurface (1000 m²)
        if (proposedParkSurface >= minParkSurface) {

            // Étape 8 : voisins dans le rayon d'accessibilité
            List<ParkProposalWork> neighbors = findNeighbors(
                    toProcess.getIdInspire(), squaresOnTerritoryMap, urbanDistance);

            // Étape 9 : construction de la proposition
            proposalResult = new ParkProposal();
            proposalResult.setAnnee(toProcess.getAnnee());
            proposalResult.setIdInspire(toProcess.getIdInspire());
            proposalResult.setParkSurface(BigDecimal.valueOf(proposedParkSurface));
            proposalResult.setCentre(toProcess.getCentre());
            proposalResult.setIsDense(toProcess.getIsDense());

            // Étape 10 : mise à jour du carreau traité (cumul, pas écrasement)
            double newTotalSurface = toProcess.getNewAccessingSurface().doubleValue() + proposedParkSurface;
            toProcess.setNewAccessingSurface(BigDecimal.valueOf(newTotalSurface));
            toProcess.setNewMissingSurface(toProcess.getNewMissingSurface()
                    .subtract(BigDecimal.valueOf(proposedParkSurface)));

            Double newSurfacePerCapita = newTotalSurface
                    / toProcess.getAccessingPopulation().doubleValue();
            toProcess.setNewSurfacePerCapita(BigDecimal.valueOf(newSurfacePerCapita));

            // Étape 11 : propagation aux voisins
            for (ParkProposalWork neighbor : neighbors) {
                double neighborNewTotalSurface = neighbor.getNewAccessingSurface().doubleValue() + proposedParkSurface;
                neighbor.setNewAccessingSurface(BigDecimal.valueOf(neighborNewTotalSurface));
                neighbor.setNewMissingSurface(
                        neighbor.getNewMissingSurface()
                                .subtract(BigDecimal.valueOf(proposedParkSurface))
                                .max(BigDecimal.ZERO));

                if (neighbor.getAccessingPopulation() != null
                        && neighbor.getAccessingPopulation().doubleValue() != 0) {
                    Double neighborNewSurfacePerCapita =
                            neighborNewTotalSurface
                                    / neighbor.getAccessingPopulation().doubleValue();
                    neighbor.setNewSurfacePerCapita(BigDecimal.valueOf(neighborNewSurfacePerCapita));
                }
            }

            log.error("Proposition pour le carre {} : ajout de parc (surface proposee: {}).",
                    toProcess.getIdInspire(), proposedParkSurface);

        } else {
            log.warn("Proposition pour le carre {} : pas d'ajout de parc (surface proposee: {}).",
                    toProcess.getIdInspire(), AbstractComputationtrategy.MIN_PARK_SURFACE);
        }
        return proposalResult;
    }
}
