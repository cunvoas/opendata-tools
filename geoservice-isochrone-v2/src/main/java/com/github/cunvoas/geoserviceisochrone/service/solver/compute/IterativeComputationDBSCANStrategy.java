package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * le carreau le plus deficitaire globalement, cette variante :</p>
 * <ol>
 *   <li>Clusterise les carreaux avec DBSCAN (densite spatiale) dans {@link #compute}</li>
 *   <li>Classe les clusters par deficit total decroissant</li>
 *   <li>Selectionne le meilleur candidat au sein du cluster le plus prioritaire</li>
 *   <li>Traite le bruit (points non clusterises) en dernier recours</li>
 * </ol>
 * <p>
 * L'objectif est de concentrer les propositions de parcs dans les zones
 * spatialement denses en deficits, maximisant l'impact par parc cree.
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

    /** Association idInspire → identifiant du cluster (ou cluster bruit). */
    private Map<String, Integer> clusterAssignment;
    /** Association identifiant cluster → liste des idInspire membres. */
    private Map<Integer, List<String>> clusterMembers;

    public IterativeComputationDBSCANStrategy(double minParkSurface) {
        this.minParkSurface = minParkSurface;
    }

    /**
     * Lance le calcul iteratif des propositions de parcs.
     * <p>
     * Etape 1 : clusterisation DBSCAN de tous les carreaux (eps = urbanDistance × 2, minPts = 3).<br>
     * Etape 2 : boucle iterative identique au parent, mais la selection du candidat
     *           privilegie les clusters les plus deficitaires.
     * </p>
     * 
     * @param carreMap carte des carreaux indexee par idInspire
     * @param minSquareMeterPerCapita seuil minimal de surface par habitant (m²/hab)
     * @param recoSquareMeterPerCapita densite recommandee (m²/hab)
     * @param urbanDistance distance d'accessibilite (m)
     * @return liste des propositions de parcs
     */
    @Override
    public List<ParkProposal> compute(Map<String, ParkProposalWork> carreMap,
                                      Double minSquareMeterPerCapita,
                                      Double recoSquareMeterPerCapita,
                                      Integer urbanDistance) {
        List<ParkProposal> proposals = new ArrayList<>();

        // Rayon de voisinage DBSCAN : 2× la distance d'accessibilité urbaine
        // (permet de former des clusters de taille cohérente avec le rayon de déplacement)
        double epsMeters = urbanDistance * 2.0;

        clusterAssignment = new HashMap<>();
        clusterMembers = new HashMap<>();

        // Convertit tous les carreaux en objets Clusterable pour Commons Math
        List<ParkProposalClusterable> allPoints = carreMap.values().stream()
                .map(ParkProposalClusterable::new)
                .collect(Collectors.toList());

        // Exécution de DBSCAN : regroupe les carreaux spatialement proches
        // Les points isolés (bruit) ne sont assignés à aucun cluster par DBSCAN
        DBSCANClusterer<ParkProposalClusterable> dbscan =
                new DBSCANClusterer<>(epsMeters, DBSCAN_MIN_PTS, new HaversineDistance());
        List<Cluster<ParkProposalClusterable>> clusters = dbscan.cluster(allPoints);

        // Indexe les clusters : chaque cluster reçoit un identifiant numérique (cid)
        Set<String> clusteredIds = new HashSet<>();
        for (int cid = 0; cid < clusters.size(); cid++) {
            List<String> ids = new ArrayList<>();
            for (ParkProposalClusterable pt : clusters.get(cid).getPoints()) {
                String id = pt.getWork().getIdInspire();
                ids.add(id);
                clusterAssignment.put(id, cid);
                clusteredIds.add(id);
            }
            clusterMembers.put(cid, ids);
        }

        // Les points non clusterisés (bruit DBSCAN) sont rassemblés dans un cluster
        // factice "bruit" qui sera traité en dernier (déficit total plus faible en moyenne)
        List<String> noiseIds = carreMap.values().stream()
                .map(ParkProposalWork::getIdInspire)
                .filter(id -> !clusteredIds.contains(id))
                .collect(Collectors.toList());

        int noiseClusterId = clusters.size();
        clusterMembers.put(noiseClusterId, noiseIds);
        for (String id : noiseIds) {
            clusterAssignment.put(id, noiseClusterId);
        }

        log.info("DBSCAN: {} clusters + noise ({} points non clusterises) pour {} carreaux, eps={}m, minPts={}",
                clusters.size(), noiseIds.size(), carreMap.size(), epsMeters, DBSCAN_MIN_PTS);

        for (int i = 0; i < carreMap.size(); i++) {
            ParkProposal proposal = calculeEtapeProposition(
                    minParkSurface, carreMap,
                    minSquareMeterPerCapita, recoSquareMeterPerCapita, urbanDistance);
            if (proposal != null) {
                proposals.add(proposal);
            } else {
                break;
            }
        }
        return proposals;
    }

    /**
     * Calcule une etape de proposition en selectionnant le candidat via DBSCAN.
     * <p>
     * Au lieu de prendre le carreau le plus deficitaire globalement, cette methode :</p>
     * <ol>
     *   <li>Classe les clusters par deficit total decroissant ({@link #rankClustersByTotalDeficit})</li>
     *   <li>Pour chaque cluster, trie ses membres par deficit individuel decroissant</li>
     *   <li>Selectionne le premier candidat du premier cluster non epuise</li>
     *   <li>Si aucun cluster n'a de candidat, fallback sur le tri global par deficit</li>
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
        // Étape 1 : tri global par déficit (fallback si aucun cluster n'a de candidat)
        List<ParkProposalWork> sorted = sortProposalsByDeficit(squaresOnTerritoryMap);

        ParkProposal proposalResult = null;
        if (!sorted.isEmpty()) {

            ParkProposalWork toProcess = null;
            // Étape 2 : classe les clusters DBSCAN par déficit total décroissant
            // (le bruit est traité comme un cluster de plus faible priorité)
            List<Integer> rankedClusters = rankClustersByTotalDeficit(squaresOnTerritoryMap);

            // Étape 3 : parcourt les clusters par priorité, cherche le premier carreau
            // encore déficitaire (newSurfacePerCapita <= minSquareMeterPerCapita)
            for (Integer cid : rankedClusters) {
                List<String> memberIds = clusterMembers.get(cid);
                if (memberIds == null || memberIds.isEmpty()) continue;

                // Au sein du cluster, trie par déficit individuel décroissant
                List<ParkProposalWork> candidates = memberIds.stream()
                        .map(squaresOnTerritoryMap::get)
                        .filter(w -> w != null
                                && w.getNewSurfacePerCapita() != null
                                && w.getNewSurfacePerCapita().doubleValue() <= minSquareMeterPerCapita)
                        .sorted(Comparator.comparing(
                                ParkProposalWork::getNewMissingSurface,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                        .collect(Collectors.toList());

                if (!candidates.isEmpty()) {
                    toProcess = candidates.get(0);
                    break;
                }
            }

            // Étape 4 : fallback — si aucun cluster n'a de candidat éligible,
            // prend le carreau le plus déficitaire globalement
            if (toProcess == null && !sorted.isEmpty()) {
                toProcess = sorted.get(0);
                if (toProcess.getNewSurfacePerCapita().doubleValue() > minSquareMeterPerCapita) {
                    return proposalResult;
                }
            }

            // Aucun carreau éligible trouvé → arrêt
            if (toProcess == null) {
                return proposalResult;
            }

            // Condition d'arrêt : le meilleur candidat est déjà au-dessus du seuil
            if (toProcess.getNewSurfacePerCapita().doubleValue() > minSquareMeterPerCapita) {
                log.info("Toutes les propositions de la commune sont traitees.");
                return proposalResult;
            }

            // Étape 5 : calcule la surface de parc nécessaire pour atteindre
            // la densité recommandée, plafonnée à CARRE_SURFACE (40 000 m²)
            Double recomputedNewAccessingSurface =
                    Math.min(
                            Math.max(
                                    recoSquareMeterPerCapita, 0),
                                AbstractComputationtrategy.CARRE_SURFACE
                            ) * toProcess.getAccessingPopulation().doubleValue();

            Double proposedParkSurface = recomputedNewAccessingSurface - toProcess.getNewAccessingSurface().doubleValue();

            // Étape 6 : seuil de validation — la surface proposée doit être ≥ minParkSurface (1000 m²)
            if (proposedParkSurface >= minParkSurface) {

                // Étape 7 : identifie les voisins dans le rayon d'accessibilité urbain
                List<ParkProposalWork> neighbors = findNeighbors(
                        toProcess.getIdInspire(), squaresOnTerritoryMap, urbanDistance);

                // Étape 8 : construction de la proposition de parc
                proposalResult = new ParkProposal();
                proposalResult.setAnnee(toProcess.getAnnee());
                proposalResult.setIdInspire(toProcess.getIdInspire());
                proposalResult.setParkSurface(BigDecimal.valueOf(proposedParkSurface));
                proposalResult.setCentre(toProcess.getCentre());
                proposalResult.setIsDense(toProcess.getIsDense());

                // Étape 9 : mise à jour du carreau traité
                // Nouvelle surface accessible = surface recommandée recalulée
                toProcess.setNewAccessingSurface(BigDecimal.valueOf(Math.round(recomputedNewAccessingSurface)));
                // Déficit restant = ancien déficit - surface ajoutée (peut devenir négatif)
                toProcess.setNewMissingSurface(toProcess.getNewMissingSurface()
                        .subtract(BigDecimal.valueOf(Math.round(recomputedNewAccessingSurface))));

                Double newSurfacePerCapita = recomputedNewAccessingSurface
                        / toProcess.getAccessingPopulation().doubleValue();
                toProcess.setNewSurfacePerCapita(BigDecimal.valueOf(newSurfacePerCapita));

                // Étape 10 : propagation aux voisins — le nouveau parc améliore
                // aussi la surface accessible des carreaux voisins
                for (ParkProposalWork neighbor : neighbors) {
                    BigDecimal neighborNewAccessingSurface = BigDecimal.valueOf(
                            neighbor.getNewAccessingSurface().doubleValue() + proposedParkSurface);
                    neighbor.setNewAccessingSurface(neighborNewAccessingSurface);
                    neighbor.setNewMissingSurface(
                            toProcess.getNewMissingSurface()
                                    .subtract(BigDecimal.valueOf(proposedParkSurface))
                                    .max(BigDecimal.ZERO));

                    if (neighbor.getAccessingPopulation() != null
                            && neighbor.getAccessingPopulation().doubleValue() != 0) {
                        Double neighborNewSurfacePerCapita =
                                neighborNewAccessingSurface.doubleValue()
                                        / neighbor.getAccessingPopulation().doubleValue();
                        neighbor.setNewSurfacePerCapita(BigDecimal.valueOf(neighborNewSurfacePerCapita));
                    }
                }

                log.error("Proposition pour le carre {} : ajout de parc (surface proposee: {}).",
                        toProcess.getIdInspire(), proposedParkSurface);

            } else {
                // Surface insuffisante : le carreau reste en l'état pour cette itération
                log.warn("Proposition pour le carre {} : pas d'ajout de parc (surface proposee: {}).",
                        toProcess.getIdInspire(), AbstractComputationtrategy.MIN_PARK_SURFACE);
            }
        }
        return proposalResult;
    }

    /**
     * Classe les clusters par deficit total decroissant.
     * <p>
     * Pour chaque cluster, somme les {@code newMissingSurface} de ses membres.
     * Les clusters vides ou a deficit nul sont classes en fin de liste.
     * En cas d'egalite, le cluster le plus nombreux est privilegie.
     * </p>
     * 
     * @param squaresOnTerritoryMap carte des carreaux (etat courant)
     * @return liste des identifiants de clusters tries par priorite
     */
    private List<Integer> rankClustersByTotalDeficit(
            Map<String, ParkProposalWork> squaresOnTerritoryMap) {
        Map<Integer, Double> clusterDeficit = new HashMap<>();

        // Somme les newMissingSurface de tous les membres de chaque cluster
        // (inclut le cluster "bruit" qui porte l'index clusters.size())
        for (Map.Entry<Integer, List<String>> entry : clusterMembers.entrySet()) {
            double total = 0.0;
            for (String id : entry.getValue()) {
                ParkProposalWork w = squaresOnTerritoryMap.get(id);
                if (w != null && w.getNewMissingSurface() != null) {
                    total += w.getNewMissingSurface().doubleValue();
                }
            }
            clusterDeficit.put(entry.getKey(), total);
        }

        // Trie les clusters par déficit total décroissant
        // En cas d'égalité, le cluster avec le plus de membres est prioritaire
        return clusterDeficit.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(e -> {
                            List<String> ids = clusterMembers.get(e.getKey());
                            return ids != null ? ids.size() : 0;
                        }, Comparator.reverseOrder()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
