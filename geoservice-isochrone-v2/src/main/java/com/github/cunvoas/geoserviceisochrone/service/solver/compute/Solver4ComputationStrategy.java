package com.github.cunvoas.geoserviceisochrone.service.solver.compute;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMax;
import org.chocosolver.solver.search.strategy.selectors.variables.FirstFail;
import org.chocosolver.solver.variables.IntVar;

import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposal;
import com.github.cunvoas.geoserviceisochrone.model.proposal.ParkProposalWork;

import lombok.extern.slf4j.Slf4j;

/**
 * Global computation using Choco solver with iterative MWIS decomposition.
 *
 * <p><strong>Algorithme :</strong></p>
 * <ol>
 *   <li>Pré-calcul du besoin propre de chaque carré en unités de {@value #UNIT_M2} m².</li>
 *   <li>Sélection MWIS gloutonne : carrés avec le plus grand déficit résiduel,
 *       mutuellement non-voisins — garantit des variables CP décorrélées.</li>
 *   <li>Résolution CP sur le sous-ensemble (~10% de la grille, quelques ms).</li>
 *   <li>Propagation de couverture : mise à jour du déficit résiduel pour tous les carrés.</li>
 *   <li>Itération jusqu'à déficit nul ou {@value #MAX_ITER} atteint.</li>
 * </ol>
 *
 * <p>Complexité : O(k × n) avec k ≤ {@value #MAX_ITER}, contre exponentiel pour le solver global.</p>
 */
@Slf4j
@Deprecated
public class Solver4ComputationStrategy extends AbstractComputationtrategy {

    /** Unité de discrétisation en m². */
    private static final int UNIT_M2 = 500;
    /** Borne haute en unités : 40 000 m² / 500 = 80 unités. */
    private static final int MAX_UNITS = (int) (CARRE_SURFACE / UNIT_M2);
    /** Seuil minimal en unités : AT_LEAST_PARK_SURFACE (1000 m²) / 500 = 2 unités. */
    private static final int MIN_UNITS = (int) Math.ceil(AT_LEAST_PARK_SURFACE / UNIT_M2);
    /** Nombre maximal d'itérations MWIS (garde-fou de convergence). */
    private static final int MAX_ITER = 20;

    @Override
    public List<ParkProposal> compute(Map<String, ParkProposalWork> squaresOnTerritoryMap,
            Double minSquareMeterPerCapita, Double recoSquareMeterPerCapita, Integer urbanDistance) {

        if (squaresOnTerritoryMap.isEmpty()) {
            log.warn("Carte des carrés vide, aucune proposition à calculer");
            return new ArrayList<>();
        }

        log.info("Démarrage Solver3 itératif pour {} carrés (unite={}m²)", squaresOnTerritoryMap.size(), UNIT_M2);

        List<String> carreIds = new ArrayList<>(squaresOnTerritoryMap.keySet());

        // Pré-calcul des voisinages
        Map<String, List<ParkProposalWork>> voisinages = new HashMap<>();
        for (String id : carreIds) {
            voisinages.put(id, findNeighbors(id, squaresOnTerritoryMap, urbanDistance));
        }

        // Passe 1 : besoin propre initial de chaque carré (en unités)
        Map<String, Integer> besoinPropreMap = computeBesoinPropre(carreIds, squaresOnTerritoryMap, recoSquareMeterPerCapita);
        log.info("Besoin propre calculé : {} zones déficitaires sur {} carrés",
            besoinPropreMap.values().stream().filter(b -> b >= MIN_UNITS).count(), carreIds.size());

        // Surface ajoutée cumulée par carré sur toutes les itérations (en m²)
        Map<String, Integer> additionsM2 = new HashMap<>();
        for (String id : carreIds) additionsM2.put(id, 0);

        // Déficit résiduel en unités, mis à jour après chaque itération de couverture
        Map<String, Integer> residualUnitsMap = new HashMap<>(besoinPropreMap);

        // Boucle MWIS itérative
        int iter = 0;
        while (iter < MAX_ITER) {
            long nbDeficit = residualUnitsMap.values().stream().filter(b -> b >= MIN_UNITS).count();
            if (nbDeficit == 0) break;

            // Sélection MWIS : carrés isolés avec le plus grand déficit résiduel (~20% max)
            int maxSelect = Math.max(1, carreIds.size() / 5);
            List<String> selectedIds = selectMwisIsolated(residualUnitsMap, voisinages, maxSelect);
            if (selectedIds.isEmpty()) break;

            log.info("Itération {}/{} : {} zones isolées sélectionnées / {} déficitaires",
                iter + 1, MAX_ITER, selectedIds.size(), nbDeficit);

            // CP solver sur le sous-ensemble (variables décorrélées → convergence en quelques ms)
            Map<String, Integer> batchM2 = solveSubset(selectedIds, residualUnitsMap, voisinages);

            // Accumulation des propositions
            boolean improved = false;
            for (Map.Entry<String, Integer> e : batchM2.entrySet()) {
                if (e.getValue() > 0) {
                    additionsM2.merge(e.getKey(), e.getValue(), Integer::sum);
                    improved = true;
                }
            }
            if (!improved) break;

            // Propagation : recalcul du déficit résiduel pour tous les carrés (couverture à 300m)
            updateResidualDeficits(residualUnitsMap, additionsM2, carreIds, squaresOnTerritoryMap,
                voisinages, recoSquareMeterPerCapita);

            iter++;
        }

        long residualZones = residualUnitsMap.values().stream().filter(b -> b >= MIN_UNITS).count();
        if (residualZones == 0) {
            log.info("Qualité OPTIMALE : tous les déficits couverts en {} itération(s).", iter);
        } else {
            log.warn("Qualité PARTIELLE après {} itération(s) : {} zones encore déficitaires.",
                iter, residualZones);
            // Passe finale : attribution directe pour les zones non atteintes par la boucle MWIS.
            // Survient quand le voisinage est dense (urbanDistance élevé) et que MAX_ITER est
            // insuffisant pour laisser toutes les zones être sélectionnées.
            log.info("Passe finale : {} zones résiduelles → attribution directe.", residualZones);
            for (String id : carreIds) {
                int residual = residualUnitsMap.getOrDefault(id, 0);
                if (residual >= MIN_UNITS) {
                    additionsM2.merge(id, residual * UNIT_M2, Integer::sum);
                }
            }
            updateResidualDeficits(residualUnitsMap, additionsM2, carreIds, squaresOnTerritoryMap,
                voisinages, recoSquareMeterPerCapita);
            long residualAfterFinal = residualUnitsMap.values().stream()
                .filter(b -> b >= MIN_UNITS).count();
            if (residualAfterFinal == 0) {
                log.info("Passe finale : couverture complète.");
            } else {
                log.warn("Après passe finale : {} zones avec déficit < {}m² (impossible à couvrir sans parc sous le seuil minimal).",
                    residualAfterFinal, MIN_UNITS * UNIT_M2);
            }
        }

        return buildProposals(carreIds, squaresOnTerritoryMap, additionsM2, voisinages);
    }

    // -------------------------------------------------------------------------
    // Passe 1 : besoin propre
    // -------------------------------------------------------------------------

    private Map<String, Integer> computeBesoinPropre(
            List<String> carreIds, Map<String, ParkProposalWork> squaresOnTerritoryMap, double reco) {
        Map<String, Integer> result = new HashMap<>();
        for (String id : carreIds) {
            ParkProposalWork c = squaresOnTerritoryMap.get(id);
            int pop  = c.getAccessingPopulation() != null ? c.getAccessingPopulation().intValue() : 0;
            int surf = c.getAccessingSurface()    != null ? c.getAccessingSurface().intValue()    : 0;
            int besoin = pop > 0
                ? (int) Math.min(Math.max(0, Math.ceil(((pop * reco) - surf) / UNIT_M2)), MAX_UNITS)
                : 0;
            result.put(id, besoin);
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Sélection MWIS gloutonne
    // -------------------------------------------------------------------------

    /**
     * Sélectionne les carrés avec le plus grand déficit résiduel en garantissant
     * qu'aucun carré sélectionné n'est dans le voisinage d'un autre sélectionné.
     * Cette propriété assure que les variables CP du sous-ensemble sont décorrélées
     * et que le solver converge en millisecondes.
     *
     * <p>Utilise le <em>besoinZone</em> = max(déficit propre, max(déficit voisins))
     * pour que les carrés sans déficit propre mais adjacents à des zones fortement
     * déficitaires soient également éligibles à la sélection (un parc placé dans
     * une zone non-déficitaire peut couvrir le voisin déficitaire).</p>
     */
    private List<String> selectMwisIsolated(
            Map<String, Integer> residualUnitsMap,
            Map<String, List<ParkProposalWork>> voisinages,
            int maxSelect) {

        // besoinZone dynamique : max(propre, max voisins)
        // Permet de sélectionner les zones non-déficitaires adjacentes à des zones déficitaires
        Map<String, Integer> besoinZoneMap = new HashMap<>();
        for (Map.Entry<String, Integer> e : residualUnitsMap.entrySet()) {
            String id = e.getKey();
            int bz = e.getValue();
            for (ParkProposalWork v : voisinages.get(id)) {
                bz = Math.max(bz, residualUnitsMap.getOrDefault(v.getIdInspire(), 0));
            }
            besoinZoneMap.put(id, bz);
        }

        List<String> sorted = besoinZoneMap.entrySet().stream()
            .filter(e -> e.getValue() >= MIN_UNITS)
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        List<String> selected = new ArrayList<>();
        Set<String> excluded  = new HashSet<>();

        for (String id : sorted) {
            if (excluded.contains(id)) continue;
            selected.add(id);
            excluded.add(id);
            for (ParkProposalWork v : voisinages.get(id)) {
                excluded.add(v.getIdInspire());
            }
            if (selected.size() >= maxSelect) break;
        }
        return selected;
    }

    // -------------------------------------------------------------------------
    // CP solver sur le sous-ensemble
    // -------------------------------------------------------------------------

    /**
     * Résout le sous-problème CP pour les carrés sélectionnés.
     * Garantie MWIS : les carrés sont mutuellement non-voisins → variables indépendantes
     * → solver trivial en quelques millisecondes quelle que soit la taille du sous-ensemble.
     *
     * @return map idInspire → surface ajoutée en m²
     */
    private Map<String, Integer> solveSubset(
            List<String> selectedIds,
            Map<String, Integer> residualUnitsMap,
            Map<String, List<ParkProposalWork>> voisinages) {

        if (selectedIds.isEmpty()) return new HashMap<>();

        Model model = new Model("Subset-" + selectedIds.size());
        Map<String, IntVar> addVars = new HashMap<>();
        List<IntVar> gapVars = new ArrayList<>();  // gap = résiduel - addVar ≥ 0 (déviation)
        int maxObj       = 0;
        int actualMaxAdd = 0;

        // Recalcul des besoinZone pour le sous-ensemble sélectionné
        // (un carré sélectionné peut avoir résiduel propre=0 mais besoinZone>0 via ses voisins)
        Map<String, Integer> besoinZoneSubset = new HashMap<>();
        for (String id : selectedIds) {
            int bz = residualUnitsMap.getOrDefault(id, 0);
            for (ParkProposalWork v : voisinages.get(id)) {
                bz = Math.max(bz, residualUnitsMap.getOrDefault(v.getIdInspire(), 0));
            }
            besoinZoneSubset.put(id, bz);
        }

        for (String id : selectedIds) {
            int residual = residualUnitsMap.getOrDefault(id, 0);
            int besoinZone = besoinZoneSubset.getOrDefault(id, residual);
            int domainUB = besoinZone >= MIN_UNITS ? besoinZone : 0;

            IntVar addVar = model.intVar("a_" + id, 0, domainUB);
            if (domainUB >= MIN_UNITS) {
                // Contrainte : 0 (pas de parc) OU >= MIN_UNITS (parc minimal)
                model.or(
                    model.arithm(addVar, "=", 0),
                    model.arithm(addVar, ">=", MIN_UNITS)
                ).post();
            }
            addVars.put(id, addVar);
            actualMaxAdd += domainUB;

            // gap = besoinZone - addVar ∈ [0, besoinZone].
            // Pour les zones sans déficit propre mais sélectionnées via besoinZone (résiduel=0),
            // on utilise besoinZone comme cible afin que le CP puisse assigner addVar > 0.
            int gapTarget = besoinZone;  // ≥ residual toujours
            IntVar gap = model.intVar("g_" + id, 0, gapTarget);
            model.arithm(gap, "+", addVar, "=", gapTarget).post();
            gapVars.add(gap);
            maxObj += gapTarget;
        }

        // Objectif lexicographique : bigM * sumGap + sumAdd
        int safeMaxObj   = Math.max(maxObj, 1);
        long idealBigML  = (long) actualMaxAdd + 1L;
        long combinedChk = idealBigML * safeMaxObj + actualMaxAdd;
        int safeBigM     = combinedChk <= Integer.MAX_VALUE
            ? (int) idealBigML
            : (int) (Integer.MAX_VALUE / Math.max(safeMaxObj, 1));
        long maxCombinedL = (long) safeBigM * safeMaxObj + actualMaxAdd;
        int maxCombined   = (int) Math.min(maxCombinedL, Integer.MAX_VALUE);

        IntVar sumGap = model.intVar("sg", 0, safeMaxObj);
        model.sum(gapVars.toArray(new IntVar[0]), "=", sumGap).post();
        IntVar sumAdd = model.intVar("sa", 0, Math.max(actualMaxAdd, 1));
        model.sum(addVars.values().toArray(new IntVar[0]), "=", sumAdd).post();
        IntVar obj = model.intVar("obj", 0, maxCombined);
        model.scalar(new IntVar[]{sumGap, sumAdd}, new int[]{safeBigM, 1}, "=", obj).post();
        model.setObjective(Model.MINIMIZE, obj);

        Solver solver = model.getSolver();
        // Filtre les variables déjà fixées à 0 (domainUB=0) — évite les nœuds triviaux
        IntVar[] decisionVars = addVars.values().stream()
            .filter(v -> v.getUB() > 0)
            .toArray(IntVar[]::new);
        solver.setSearch(Search.intVarSearch(new FirstFail(model), new IntDomainMax(), decisionVars));
        solver.limitTime(10_000); // 10s max par sous-problème (largement suffisant)

        Solution best = null;
        while (solver.solve()) {
            best = new Solution(model).record();
        }
        log.debug("Sous-problème {}: solutions={}, nœuds={}, temps={}ms",
            selectedIds.size(),
            solver.getSolutionCount(),
            solver.getNodeCount(),
            solver.getTimeCount());

        Map<String, Integer> result = new HashMap<>();
        if (best != null) {
            try {
                best.restore();
                for (String id : selectedIds) {
                    result.put(id, addVars.get(id).getValue() * UNIT_M2);
                }
            } catch (ContradictionException e) {
                log.error("Erreur restauration sous-problème — fallback glouton", e);
                fallbackGreedy(selectedIds, besoinZoneSubset, result);
            }
        } else {
            log.warn("Sous-problème sans solution CP — fallback glouton pour {} carrés", selectedIds.size());
            fallbackGreedy(selectedIds, besoinZoneSubset, result);
        }
        return result;
    }

    /**
     * Affectation gloutonne de secours : attribue directement le besoinZone résiduel.
     * Le paramètre {@code besoinZoneMap} est max(résiduel propre, max résiduel voisins)
     * afin que les zones non-déficitaires sélectionnées via un voisin puissent
     * recevoir un parc.
     */
    private void fallbackGreedy(List<String> ids, Map<String, Integer> besoinZoneMap,
            Map<String, Integer> result) {
        for (String id : ids) {
            int bz = besoinZoneMap.getOrDefault(id, 0);
            result.put(id, bz >= MIN_UNITS ? bz * UNIT_M2 : 0);
        }
    }

    // -------------------------------------------------------------------------
    // Propagation de couverture
    // -------------------------------------------------------------------------

    /**
     * Recalcule le déficit résiduel pour tous les carrés après placement des parcs.
     * totalSurface(X) = surfaceExist(X) + Σ additionsM2 pour X et ses voisins
     * residualUnits(X) = max(0, ceil(pop × reco / UNIT_M2) − totalUnits(X))
     */
    private void updateResidualDeficits(
            Map<String, Integer> residualUnitsMap,
            Map<String, Integer> additionsM2,
            List<String> carreIds,
            Map<String, ParkProposalWork> squaresOnTerritoryMap,
            Map<String, List<ParkProposalWork>> voisinages,
            double reco) {

        for (String id : carreIds) {
            ParkProposalWork c = squaresOnTerritoryMap.get(id);
            int pop  = c.getAccessingPopulation() != null ? c.getAccessingPopulation().intValue() : 0;
            int surf = c.getAccessingSurface()    != null ? c.getAccessingSurface().intValue()    : 0;
            if (pop == 0) {
                residualUnitsMap.put(id, 0);
                continue;
            }
            int totalAddedM2 = additionsM2.getOrDefault(id, 0);
            for (ParkProposalWork v : voisinages.get(id)) {
                totalAddedM2 += additionsM2.getOrDefault(v.getIdInspire(), 0);
            }
            // Formule cohérente avec computeBesoinPropre :
            // ceil((pop×reco - surf - totalAdded) / UNIT_M2)
            // NB: ceil(A-B) ≠ ceil(A)-ceil(B) → utiliser une seule fraction pour éviter
            // la sous-estimation qui laisse des zones définitivement ignorées.
            double trueDeficitM2 = (pop * reco) - surf - totalAddedM2;
            residualUnitsMap.put(id, Math.min(Math.max(0, (int) Math.ceil(trueDeficitM2 / UNIT_M2)), MAX_UNITS));
        }
    }

    // -------------------------------------------------------------------------
    // Construction des résultats 
    // -------------------------------------------------------------------------

    private List<ParkProposal> buildProposals(
            List<String> carreIds,
            Map<String, ParkProposalWork> squaresOnTerritoryMap,
            Map<String, Integer> additionsM2,
            Map<String, List<ParkProposalWork>> voisinages) {

        List<ParkProposal> proposals = new ArrayList<>();

        for (String idInspire : carreIds) {
            ParkProposalWork carre = squaresOnTerritoryMap.get(idInspire);
            int addedM2 = additionsM2.getOrDefault(idInspire, 0);

            if (addedM2 > 0) {
                ParkProposal p = new ParkProposal();
                p.setAnnee(carre.getAnnee());
                p.setIdInspire(carre.getIdInspire());
                p.setIsDense(carre.getIsDense());
                p.setCentre(carre.getCentre());
                p.setParkSurface(BigDecimal.valueOf(addedM2));
                proposals.add(p);
            }

            double totalAddedM2 = addedM2;
            for (ParkProposalWork v : voisinages.get(idInspire)) {
                totalAddedM2 += additionsM2.getOrDefault(v.getIdInspire(), 0);
            }

            double surfaceBaseM2  = carre.getAccessingSurface()    != null ? carre.getAccessingSurface().doubleValue()    : 0d;
            double population     = carre.getAccessingPopulation() != null ? carre.getAccessingPopulation().doubleValue() : 0d;
            double totalSurfaceM2 = surfaceBaseM2 + totalAddedM2;

            if (population > 0) {
                carre.setNewSurfacePerCapita(BigDecimal.valueOf(totalSurfaceM2 / population));
                carre.setSurfacePerCapita(BigDecimal.valueOf(totalSurfaceM2 / population));
            } else {
                carre.setNewSurfacePerCapita(null);
            }

            BigDecimal missing = carre.getMissingSurface() != null ? carre.getMissingSurface() : BigDecimal.ZERO;
            carre.setNewMissingSurface(missing.subtract(BigDecimal.valueOf(totalAddedM2)).max(BigDecimal.ZERO));
            carre.setNewAccessingSurface(BigDecimal.valueOf(addedM2));
        }

        log.info("Résolution terminée : {} propositions retenues.", proposals.size());
        return proposals;
    }
}
