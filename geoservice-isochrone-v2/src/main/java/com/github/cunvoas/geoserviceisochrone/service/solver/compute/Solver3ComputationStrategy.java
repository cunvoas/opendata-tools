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
 * Stratégie de calcul CP itérative utilisant une décomposition MWIS (Maximum Weight
 * Independent Set) pour placer les parcs proposés dans les carrés de la grille.
 *
 * <p><strong>Algorithme :</strong></p>
 * <ol>
 *   <li>Pré-calcul du <em>besoin propre</em> de chaque carré en unités de {@value #UNIT_M2} m²
 *       : {@code ceil((pop × reco − surfExist) / UNIT_M2)}.</li>
 *   <li>Sélection MWIS gloutonne : carrés avec le plus grand <em>besoinZone</em>
 *       (= max(besoin propre, max besoin des voisins)), mutuellement non-voisins
 *       — garantit que les variables CP du sous-ensemble sont décorrélées et que
 *       les zones sans déficit propre mais adjacentes à des zones déficitaires
 *       sont également éligibles.</li>
 *   <li>Résolution CP sur le sous-ensemble (~20 % max de la grille, quelques ms).</li>
 *   <li>Propagation : recalcul du déficit résiduel pour tous les carrés en tenant
 *       compte des parcs nouvellement placés et de leurs voisins à portée.</li>
 *   <li>Itération jusqu'à déficit nul ou {@value #MAX_ITER} atteint.</li>
 *   <li>Passe finale directe si des zones restent déficitaires après la boucle
 *       (cas dense : voisinage élevé bloquant la sélection MWIS).</li>
 * </ol>
 *
 * <p>Complexité : O(k × n) avec k ≤ {@value #MAX_ITER}, contre exponentiel pour un
 * modèle CP global.</p>
 *
 * @see SolverV3ComputationStrategy modèle CP global de référence (ne pas modifier)
 */
@Slf4j
public class Solver3ComputationStrategy extends AbstractComputationtrategy {

    /** Unité de discrétisation en m². */
    private static final int UNIT_M2 = 500;
    /** Borne haute en unités : 40 000 m² / 500 = 80 unités. */
    private static final int MAX_UNITS = (int) (CARRE_SURFACE / UNIT_M2);
    /** Seuil minimal en unités : AT_LEAST_PARK_SURFACE (1000 m²) / 500 = 2 unités. */
    private static final int MIN_UNITS = (int) Math.ceil(AT_LEAST_PARK_SURFACE / UNIT_M2);
    /** Nombre maximal d'itérations MWIS (garde-fou de convergence). */
    private static final int MAX_ITER = 20;

    /**
     * Calcule les propositions de parcs pour combler les déficits de surface verte
     * par habitant sur l'ensemble des carrés de la zone d'étude.
     *
     * @param squaresOnTerritoryMap                 carrés de la grille, indexés par idInspire
     * @param minSquareMeterPerCapita  seuil minimal de surface verte par habitant (m²/hab)
     * @param recoSquareMeterPerCapita objectif recommandé de surface verte par habitant (m²/hab)
     * @param urbanDistance            rayon de voisinage en mètres (portée d'un parc)
     * @return liste des propositions de parcs, une entrée par carré recevant un ajout de surface
     */
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

        // --- Passe initiale : besoin propre de chaque carré (en unités de UNIT_M2) ---
        Map<String, Integer> besoinPropreMap = computeBesoinPropre(carreIds, squaresOnTerritoryMap, recoSquareMeterPerCapita);
        log.info("Besoin propre calculé : {} zones déficitaires sur {} carrés",
            besoinPropreMap.values().stream().filter(b -> b >= MIN_UNITS).count(), carreIds.size());

        // Surface ajoutée cumulée (m²) par carré, toutes itérations confondues.
        Map<String, Integer> additionsM2 = new HashMap<>();
        for (String id : carreIds) additionsM2.put(id, 0);

        // Déficit résiduel en unités, recalculé après chaque itération.
        // Initialisé à besoinPropre, puis décrémenté au fil des placements de parcs.
        Map<String, Integer> residualUnitsMap = new HashMap<>(besoinPropreMap);

        // --- Boucle MWIS itérative ---
        int iter = 0;
        while (iter < MAX_ITER) {
            long nbDeficit = residualUnitsMap.values().stream().filter(b -> b >= MIN_UNITS).count();
            if (nbDeficit == 0) break;

            // Sélection MWIS : jusqu'à 20 % des carrés, mutuellement non-voisins,
            // classés par besoinZone décroissant (inclut les zones sans déficit propre
            // mais utiles comme support pour couvrir un voisin déficitaire).
            int maxSelect = Math.max(1, carreIds.size() / 5);
            List<String> selectedIds = selectMwisIsolated(residualUnitsMap, voisinages, maxSelect);
            if (selectedIds.isEmpty()) break;

            log.info("Itération {}/{} : {} zones isolées sélectionnées / {} déficitaires",
                iter + 1, MAX_ITER, selectedIds.size(), nbDeficit);

            // Résolution CP sur le sous-ensemble (variables décorrélées → solution en quelques ms).
            Map<String, Integer> batchM2 = solveSubset(selectedIds, residualUnitsMap, voisinages);

            // Accumulation : fusion des surfaces proposées dans le résultat global.
            boolean improved = false;
            for (Map.Entry<String, Integer> e : batchM2.entrySet()) {
                if (e.getValue() > 0) {
                    additionsM2.merge(e.getKey(), e.getValue(), Integer::sum);
                    improved = true;
                }
            }
            // Aucune amélioration → convergence atteinte prématurément, sortie de boucle.
            if (!improved) break;

            // Propagation : recalcul du déficit résiduel global en tenant compte
            // de la portée de voisinage (urbanDistance + 100 m dans findNeighbors).
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
            // Passe finale de rattrapage : attribution directe du déficit résiduel.
            // Cause typique : voisinage très dense (urbanDistance élevé) → une zone X est
            // exclue de la sélection MWIS à chaque itération par ses nombreux voisins déjà
            // sélectionnés → MAX_ITER atteint sans que X soit jamais couverte.
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

    /**
     * Calcule le besoin initial de chaque carré en unités de {@value #UNIT_M2} m².
     *
     * <p>Formule : {@code ceil((pop × reco − surfExist) / UNIT_M2)}, borné dans
     * {@code [0, MAX_UNITS]}. Retourne 0 pour les carrés sans population.</p>
     *
     * @param carreIds liste des identifiants à traiter
     * @param squaresOnTerritoryMap données des carrés (population, surface existante)
     * @param reco     objectif recommandé en m² par habitant
     * @return map idInspire → besoin propre en unités (0 si pas de déficit)
     */
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
     * Sélection MWIS gloutonne : retourne un ensemble indépendant (aucun carré
     * sélectionné n'est voisin d'un autre) trié par <em>besoinZone</em> décroissant.
     *
     * <p>{@code besoinZone(X) = max(résiduel(X), max résiduel(voisins(X)))} : cela
     * permet de sélectionner des zones sans déficit propre (résiduel = 0) dont un
     * parc couvrirait un voisin fortement déficitaire. Sans cette règle, ces zones
     * ne seraient jamais éligibles et certains déficits resteraient irrésolus.</p>
     *
     * <p>La propriété d'ensemble indépendant garantit que les variables CP associées
     * sont décorrélées, ce qui rend le sous-problème trivial pour le solver Choco.</p>
     *
     * @param residualUnitsMap déficit résiduel courant en unités par carré
     * @param voisinages       voisinage pré-calculé (idInspire → liste voisins)
     * @param maxSelect        nombre maximal de carrés à sélectionner
     * @return liste ordonnée par besoinZone décroissant, au plus {@code maxSelect} éléments
     */
    private List<String> selectMwisIsolated(
            Map<String, Integer> residualUnitsMap,
            Map<String, List<ParkProposalWork>> voisinages,
            int maxSelect) {

        // besoinZone = max(propre, max voisins) : rend éligibles les zones sans déficit
        // propre mais utiles comme support de parc pour couvrir un voisin déficitaire.
        Map<String, Integer> besoinZoneMap = new HashMap<>();
        for (Map.Entry<String, Integer> e : residualUnitsMap.entrySet()) {
            String id = e.getKey();
            int bz = e.getValue();
            for (ParkProposalWork v : voisinages.get(id)) {
                bz = Math.max(bz, residualUnitsMap.getOrDefault(v.getIdInspire(), 0));
            }
            besoinZoneMap.put(id, bz);
        }

        // Candidats triés par besoinZone décroissant, seuil MIN_UNITS appliqué.
        // Seuls les carrés avec besoinZone ≥ MIN_UNITS sont retenus : en dessous,
        // le solver ne peut pas placer de parc (taille minimale imposée).
        List<String> sorted = besoinZoneMap.entrySet().stream()
            .filter(e -> e.getValue() >= MIN_UNITS)     // exclut les zones sans besoin significatif
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()) // priorité au déficit max
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());

        // Construction de l'ensemble indépendant (greedy MWIS) :
        // selected = carrés retenus (mutuellement non-voisins)
        // excluded = carrés déjà sélectionnés + tous leurs voisins (ne peuvent plus être choisis)
        List<String> selected = new ArrayList<>();
        Set<String> excluded  = new HashSet<>();

        for (String id : sorted) {
            if (excluded.contains(id)) continue; // id déjà bloqué par un voisin sélectionné
            selected.add(id);
            excluded.add(id);                    // id lui-même ne peut plus être repris
            for (ParkProposalWork v : voisinages.get(id)) {
                excluded.add(v.getIdInspire()); // voisins bloqués : garantit l'indépendance
            }
            if (selected.size() >= maxSelect) break; // quota atteint → s'arrêter
        }
        return selected;
    }

    // -------------------------------------------------------------------------
    // CP solver sur le sous-ensemble
    // -------------------------------------------------------------------------

    /**
     * Résout le sous-problème CP pour les carrés sélectionnés par la sélection MWIS.
     *
     * <p>Grâce à la garantie MWIS (carrés mutuellement non-voisins), les variables
     * CP sont indépendantes et le solver converge en quelques millisecondes quelle
     * que soit la taille du sous-ensemble.</p>
     *
     * <p>Modèle CP :</p>
     * <ul>
     *   <li>{@code addVar ∈ {0} ∪ [MIN_UNITS, besoinZone]} pour chaque carré.</li>
     *   <li>{@code gap + addVar = besoinZone} avec {@code gap ≥ 0} (déviation résiduelle).</li>
     *   <li>Objectif : minimiser {@code bigM × Σgap + ΣaddVar} → maximise la couverture
     *       tout en préférant les parcs les plus petits.</li>
     * </ul>
     * <p>Note : la cible {@code gap = besoinZone} (et non {@code résiduel}) est critique
     * pour les zones sans déficit propre : avec {@code résiduel=0}, une cible {@code 0}
     * forcerait {@code addVar=0}, empêchant tout placement.</p>
     *
     * @param selectedIds      identifiants des carrés sélectionnés (ensemble indépendant MWIS)
     * @param residualUnitsMap déficit résiduel courant en unités par carré
     * @param voisinages       voisinage pré-calculé (idInspire → liste voisins)
     * @return map idInspire → surface ajoutée en m² (0 si aucun parc proposé)
     */
    private Map<String, Integer> solveSubset(
            List<String> selectedIds,
            Map<String, Integer> residualUnitsMap,
            Map<String, List<ParkProposalWork>> voisinages) {

        if (selectedIds.isEmpty()) return new HashMap<>();

        Model model = new Model("Subset-" + selectedIds.size());

        // addVars  : variable CP principale — unités de UNIT_M2 à ajouter dans le carré i.
        //            domaine : {0} ∪ [MIN_UNITS, besoinZone(i)]
        Map<String, IntVar> addVars = new HashMap<>();

        // gapVars  : variable d'écart — gap(i) = besoinZone(i) - addVar(i) ∈ [0, besoinZone(i)].
        //            gap=0 signifie déficit comblé, gap>0 signifie déficit résiduel.
        List<IntVar> gapVars = new ArrayList<>();

        // maxObj       : somme des besoinZone → borne haute de sumGap (toutes les variables gap)
        // actualMaxAdd : somme des domainUB → borne haute de sumAdd (toutes les variables addVar)
        int maxObj       = 0;
        int actualMaxAdd = 0;

        // besoinZoneSubset : recalcul pour les seuls carrés sélectionnés.
        // Un carré peut avoir résiduel propre=0 mais besoinZone>0 s'il est adjacent
        // à un carré déficitaire → son addVar doit pouvoir prendre une valeur > 0.
        Map<String, Integer> besoinZoneSubset = new HashMap<>();
        for (String id : selectedIds) {
            int bz = residualUnitsMap.getOrDefault(id, 0);
            for (ParkProposalWork v : voisinages.get(id)) {
                bz = Math.max(bz, residualUnitsMap.getOrDefault(v.getIdInspire(), 0));
            }
            besoinZoneSubset.put(id, bz);
        }

        for (String id : selectedIds) {
            // residual   : déficit propre du carré (en unités). Peut être 0 si le carré
            //              est sélectionné pour son besoinZone (support d'un voisin déficitaire).
            int residual   = residualUnitsMap.getOrDefault(id, 0);

            // besoinZone : max(residual, max des residuals voisins).
            //              Représente l'utilité de placer un parc ici, que ce soit pour
            //              couvrir le carré lui-même ou pour couvrir un voisin via propagation.
            int besoinZone = besoinZoneSubset.getOrDefault(id, residual);

            // domainUB   : borne haute de addVar.
            //              Mis à 0 si besoinZone < MIN_UNITS : on ne peut pas placer un parc
            //              sous la surface minimale autorisée → variable fixée à 0 d'office.
            int domainUB   = besoinZone >= MIN_UNITS ? besoinZone : 0;

            // addVar     : nombre d'unités (×UNIT_M2) à ajouter dans ce carré.
            //              Domaine [0, domainUB] avant ajout de la contrainte binaire.
            IntVar addVar = model.intVar("a_" + id, 0, domainUB);
            if (domainUB >= MIN_UNITS) {
                // Contrainte de seuil minimal : soit pas de parc (0), soit un parc d'au
                // moins MIN_UNITS unités. Interdit les valeurs intermédiaires [1, MIN_UNITS-1]
                // qui correspondraient à des parcs inférieurs à AT_LEAST_PARK_SURFACE.
                model.or(
                    model.arithm(addVar, "=", 0),
                    model.arithm(addVar, ">=", MIN_UNITS)
                ).post();
            }
            addVars.put(id, addVar);
            actualMaxAdd += domainUB; // accumulation de la borne haute globale de sumAdd

            // gapTarget  : cible de la contrainte d'écart = besoinZone (et non residual).
            //              Critique pour les zones support (residual=0, besoinZone>0) :
            //              avec gapTarget=residual=0 on forcerait addVar=0 → aucun parc placé.
            //              Avec gapTarget=besoinZone, addVar peut prendre n'importe quelle
            //              valeur dans [0, besoinZone] → le CP peut choisir de placer un parc.
            int gapTarget = besoinZone; // toujours ≥ residual

            // gap        : déviation résiduelle après placement.
            //              gap=0 → déficit (besoinZone) entièrement couvert.
            //              gap>0 → déficit partiellement couvert, réduit à gap unités.
            IntVar gap = model.intVar("g_" + id, 0, gapTarget);
            model.arithm(gap, "+", addVar, "=", gapTarget).post(); // gap + addVar = besoinZone
            gapVars.add(gap);
            maxObj += gapTarget; // contribution à la borne haute de sumGap
        }

        // --- Construction de la fonction objectif (lexicographique encodée en scalaire) ---
        //
        // But : minimiser bigM × Σgap + ΣaddVar
        //
        //   • Σgap   (priorité haute) : somme des déviations résiduelles.
        //             Minimiser Σgap = maximiser la couverture des déficits.
        //   • ΣaddVar (priorité basse) : somme des surfaces ajoutées.
        //             À couverture égale, préférer les parcs les plus petits.
        //
        //   bigM garantit la lexicographie : réduire Σgap d'1 unité vaut toujours plus
        //   qu'augmenter ΣaddVar de n'importe quelle valeur.
        //   Valeur idéale : bigM = actualMaxAdd + 1 (toujours > ΣaddVar max).

        // safeMaxObj : borne haute de sumGap (= maxObj = Σ besoinZone). Minimum 1 pour éviter /0.
        int safeMaxObj = Math.max(maxObj, 1);

        // idealBigML : bigM idéal = actualMaxAdd + 1.
        //              Garantit bigM × (Σgap - 1) > ΣaddVar max → vraie lexicographie.
        long idealBigML = (long) actualMaxAdd + 1L;

        // combinedChk : valeur maximale possible de l'objectif avec bigM idéal.
        //               Si elle dépasse Integer.MAX_VALUE, on réduit bigM pour éviter l'overflow.
        long combinedChk = idealBigML * safeMaxObj + actualMaxAdd;
        int safeBigM = combinedChk <= Integer.MAX_VALUE
            ? (int) idealBigML
            : (int) (Integer.MAX_VALUE / Math.max(safeMaxObj, 1)); // bigM réduit anti-overflow

        // maxCombined : borne haute de la variable objectif obj (en tenant compte du bigM effectif).
        long maxCombinedL = (long) safeBigM * safeMaxObj + actualMaxAdd;
        int maxCombined   = (int) Math.min(maxCombinedL, Integer.MAX_VALUE);

        // sumGap  : agrégat Σgap(i), domaine [0, safeMaxObj].
        IntVar sumGap = model.intVar("sg", 0, safeMaxObj);
        model.sum(gapVars.toArray(new IntVar[0]), "=", sumGap).post();

        // sumAdd  : agrégat ΣaddVar(i), domaine [0, actualMaxAdd].
        IntVar sumAdd = model.intVar("sa", 0, Math.max(actualMaxAdd, 1));
        model.sum(addVars.values().toArray(new IntVar[0]), "=", sumAdd).post();

        // obj     : variable objectif scalaire — encode bigM×sumGap + sumAdd.
        //           Minimiser obj = résoudre la lexicographie (couverture d'abord, économie ensuite).
        IntVar obj = model.intVar("obj", 0, maxCombined);
        model.scalar(new IntVar[]{sumGap, sumAdd}, new int[]{safeBigM, 1}, "=", obj).post();
        model.setObjective(Model.MINIMIZE, obj);

        Solver solver = model.getSolver();

        // decisionVars : seules les variables avec domaine non trivial (UB > 0) nécessitent
        //                du branchement. Les variables fixées à 0 (domainUB=0) sont ignorées
        //                pour éviter des nœuds de recherche inutiles.
        IntVar[] decisionVars = addVars.values().stream()
            .filter(v -> v.getUB() > 0)
            .toArray(IntVar[]::new);

        // Stratégie : FirstFail (variable de plus petit domaine en premier = moins de backtrack)
        //           + IntDomainMax (essayer la valeur max en premier = tenter de placer un grand parc).
        solver.setSearch(Search.intVarSearch(new FirstFail(model), new IntDomainMax(), decisionVars));
        solver.limitTime(10_000); // garde-fou : 10 s max par sous-problème (ms en pratique)

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
     * Affectation gloutonne de secours, utilisée quand le solver CP ne trouve pas
     * de solution pour le sous-ensemble considéré.
     *
     * <p>Attribue directement {@code besoinZone × UNIT_M2} à chaque carré dont le
     * besoinZone est ≥ {@value #MIN_UNITS}. Garantit une couverture minimale même
     * en cas d'échec du solver (timeout, contradiction inattendue).</p>
     *
     * @param ids           identifiants des carrés à traiter
     * @param besoinZoneMap besoinZone par carré (max résiduel propre + max voisins)
     * @param result        map de résultat à alimenter (idInspire → m² ajoutés)
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
     * Recalcule le déficit résiduel pour tous les carrés après chaque itération
     * de placement de parcs, en tenant compte de la couverture croisée par voisinage.
     *
     * <p>Formule :
     * <pre>
     *   totalSurface(X) = surfExist(X) + additionsM2(X) + Σ additionsM2(voisins(X))
     *   résiduel(X)     = max(0, ceil((pop(X) × reco − totalSurface(X)) / UNIT_M2))
     * </pre>
     * La même fraction est utilisée d'un bout à l'autre (pas de double arrondi)
     * pour être cohérent avec {@code computeBesoinPropre} et éviter une sous-estimation
     * qui laisserait des zones définitivement ignorées.</p>
     *
     * @param residualUnitsMap déficit résiduel à mettre à jour (modifié en place)
     * @param additionsM2      surfaces ajoutées cumulées par carré (en m²)
     * @param carreIds         liste complète des identifiants
     * @param squaresOnTerritoryMap         données des carrés (population, surface existante)
     * @param voisinages       voisinage pré-calculé (idInspire → liste voisins)
     * @param reco             objectif recommandé en m² par habitant
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
            // Formule identique à computeBesoinPropre, appliquée sur la surface totale
            // (existante + voisinage) pour éviter le double arrondi ceil(A)-ceil(B) ≠ ceil(A-B)
            // qui sous-estime et peut rendre un résiduel = 1 invisible (< MIN_UNITS = 2).
            double trueDeficitM2 = (pop * reco) - surf - totalAddedM2;
            residualUnitsMap.put(id, Math.min(Math.max(0, (int) Math.ceil(trueDeficitM2 / UNIT_M2)), MAX_UNITS));
        }
    }

    // -------------------------------------------------------------------------
    // Construction des résultats
    // -------------------------------------------------------------------------

    /**
     * Construit la liste finale des propositions de parcs à partir des surfaces
     * accumulées et met à jour les métriques des carrés (surface/habitant, déficit
     * résiduel).
     *
     * @param carreIds    liste des identifiants à traiter
     * @param squaresOnTerritoryMap    données des carrés (modifiées en place pour les métriques)
     * @param additionsM2 surface ajoutée cumulée par carré (en m²)
     * @param voisinages  voisinage pré-calculé (idInspire → liste voisins)
     * @return liste des {@link ParkProposal} pour les carrés recevant un ajout > 0
     */
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
