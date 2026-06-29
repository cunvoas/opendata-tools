# Rétro-documentation : `ProposalComputationStrategy`

## Vue d'ensemble

Pattern **Strategy** pour le calcul des propositions de création de parcs urbains. Détermine, pour chaque carré INSEE 200m, la surface de parc à ajouter pour combler le déficit vert par habitant.

```
ServicePropositionParc.calculeProposition()
  └── ProposalComputationStrategyFactory.create(typeAlgo, MIN_PARK_SURFACE)
        ├── ITERATIVE_2 → IterativeComputationDeficit2Strategy
        ├── ITERATIVE_3 → IterativeComputationPopulation1Strategy
        ├── PPC_3       → Solver3ComputationStrategy (MWIS + Choco CP)
        ├── CHI2_6      → LeastSquaresNeighbour2Strategy
        └── GENETIC_7   → Genetic1Strategy (commons-math3 GA)
```

**5 algorithmes actifs** (dans `getAvailableTypes()`) sur 9 implémentés. Les autres (`ITERATIVE_1`, `PPC_1`, `PPC_2`, `CHI2_5`) sont disponibles dans la factory `create()` mais commentés dans la liste des types disponibles.

---

## Interface

**Fichier :** `service/solver/compute/ProposalComputationStrategy.java`

```java
List<ParkProposal> compute(Map<String, ParkProposalWork> carreMap,
                           Double minSquareMeterPerCapita,
                           Double recoSquareMeterPerCapita,
                           Integer urbanDistance);
```

- `carreMap` : carrés INSEE 200m de la commune, indexés par `idInspire`, avec population, surface existante, déficit
- `minSquareMeterPerCapita` : seuil minimal sous lequel un carré est déficitaire (ex: 8 urbain / 10 péri-urbain)
- `recoSquareMeterPerCapita` : objectif OMS (ex: 12 m²/hab)
- `urbanDistance` : rayon d'accessibilité (300m dense / 1200m péri-urbain)

---

## Classe abstraite : `AbstractComputationtrategy`

**Fichier :** `AbstractComputationtrategy.java`

Constantes communes :
- `AT_LEAST_PARK_SURFACE = 1_000` m² — seuil de proposition acceptable
- `MIN_PARK_SURFACE = 650` m² — seuil minimum (utilisé par la factory)
- `CARRE_SIZE = 200` m — côté d'un carré INSEE
- `CARRE_SURFACE = 40_000` m² — surface d'un carré

Méthodes de tri partagées (délèguent à `ProposalSortStrategyFactory`) :
- `sortProposalsByDeficit()` → tri par `newMissingSurface` décroissant
- `sortProposalsByPersona()` → tri par persona
- `sortProposalsByMissingPopulation()` → tri par `newMissingSurface × population`

Voisinage : `findNeighbors(id, carreMap, urbanDistance)` → `ParkProposalHelper.findNeighbors()` (distance centroïde < `urbanDistance + 100m`)

---

## Algorithme 1 : `ITERATIVE_2` — Itératif par déficit v2

**Classe :** `IterativeComputationDeficit2Strategy`

### Logique

Boucle `for (i = 0; i < carreMap.size(); i++)` — une itération par carreau max.

Chaque itération :
1. Trier les carrés par **déficit surfacique** décroissant (`sortProposalsByDeficit`)
2. Prendre le premier (le pire)
3. Si sa densité > `minSquareMeterPerCapita` → arrêt (tout le monde est au-dessus du seuil)
4. Calculer `newParkSurface = min(max(reco - spc, 0), CARRE_SURFACE) × population`
5. Si `≥ minParkSurface` → créer la proposition
6. Propager `newParkSurface` aux voisins : leur surface totale accessible augmente, leur `surfacePerCapita` est recalculée

### Particularité v2 vs v1

- `newSurface` = `BigDecimal.valueOf(newParkSurface)` (delta)
- la mise à jour voisins utilise `neighborNewTotalSurface = neighbor.getAccessingSurface() + newParkSurface` (total cumulé)
- `setNewMissingSurface` du voisin est basé sur **`toProcess.getNewMissingSurface()`** (celle du carreau central après soustraction) — **probable bug**, devrait être la `newMissingSurface` propre du voisin

---

## Algorithme 2 : `ITERATIVE_3` — Itératif par population v2

**Classe :** `IterativeComputationPopulation1Strategy`

### Différence avec ITERATIVE_2

Le critère de sélection du carreau à traiter :
- **ITERATIVE_2** : tri par `newMissingSurface` (déficit pur)
- **ITERATIVE_3** : tri par `missingPopulation` = `newMissingSurface × accessingPopulation` (impact humain)

Privilégie les quartiers denses à fort déficit total plutôt que les zones à fort déficit par habitant mais peu peuplées.

Le reste de l'algorithme (calcul surface, propagation, mises à jour) est identique à ITERATIVE_2.

---

## Algorithme 3 : `PPC_3` — Solveur PPC v3 (MWIS + Choco CP)

    MWIS: Maximum Weight Independent Set
    Choco: (site)[https://choco-solver.org/]

**Classe :** `Solver3ComputationStrategy`

598 lignes. L'algo le plus sophistiqué.

### Pipeline

```
1. computeBesoinPropre() → besoin en unités de 500m² par carré
2. Boucle MWIS (max 20 itérations) :
   a. selectMwisIsolated() → sous-ensemble de carrés mutuellement non-voisins (~20%)
   b. solveSubset() → modèle Choco CP sur le sous-ensemble
   c. updateResidualDeficits() → propagation de couverture
3. Passe finale directe pour les zones résiduelles bloquées
4. buildProposals() → construction des résultats
```

### Discrétisation

- `UNIT_M2 = 500` m² — granularité de calcul
- `MAX_UNITS = 80` — 40 000 / 500
- `MIN_UNITS = 2` — ceil(1000 / 500)

### Sélection MWIS

`besoinZone(X) = max(résiduel(X), max résiduel(voisins(X)))` :
- rend éligibles les zones sans déficit propre mais utiles comme support de parc
- sélection gloutonne : tri par besoinZone décroissant, exclusion des voisins
- max 20% des carrés par itération

### Modèle CP (Choco Solver)

Pour chaque carré sélectionné :
- `addVar ∈ {0} ∪ [MIN_UNITS, besoinZone]` — surface à ajouter en unités
- `gap + addVar = besoinZone` — déviation résiduelle
- Objectif : `minimize(bigM × Σgap + ΣaddVar)`
  - priorité haute : minimiser les écarts résiduels
  - priorité basse : minimiser la somme des surfaces ajoutées

### Fallback

Si le solver CP échoue (timeout / contradiction) → `fallbackGreedy()` : attribue `besoinZone × UNIT_M2` à chaque carré.

### Passe finale

Quand des zones restent déficitaires après MAX_ITER (cas dense où une zone est bloquée par ses voisins MWIS), attribution directe du déficit résiduel.

---

## Algorithme 4 : `CHI2_6` — χ² avec voisins v3

**Classe :** `LeastSquaresNeighbour2Strategy`

### Approche

Hybride : **χ² (moindres carrés pondérés) comme filtre de convergence**, **déficit complet pour le montant**.

### Boucle

`maxIterations = carreMap.size() × 100` (marge large car le pas χ² est petit)

Chaque itération :
1. **Observations χ²** : carreaux avec `population > 0` et `surfacePerCapita ≤ minSquareMeterPerCapita`
2. **Fit degré 0** (modèle constant) : `PolynomialCurveFitter.create(0)` → moyenne pondérée par la population des deficits
3. **Sélection** : carreau avec le plus grand `newMissingSurface` (tri par déficit)
4. **Montant** : `newParkSurface = min(deficitPerCapita × population, CARRE_SURFACE)` — **déficit complet** (pas le χ²)
5. **Proposition** si `≥ MIN_PARK_SURFACE`
6. **Propagation** : `accessingSurface` est **muté** (invariant clé) pour garantir la cohérence des itérations futures

### Correctifs v3 vs v1/v2

- Le χ² n'est **pas** utilisé comme plafond du montant (corrige une sous-estimation de ~38%)
- `accessingSurface` est muté à chaque étape (cohérence itérative)
- `surfacePerCapita` mis à jour chez les voisins pour que le filtre χ² détecte correctement la convergence
- `newMissingSurface` basé sur la valeur **propre du voisin** (et non celle du carreau central, bug de v2)

---

## Algorithme 5 : `GENETIC_7` — Génétique v2

**Classe :** `Genetic1Strategy`

### Paramètres

| Paramètre | Valeur |
|---|---|
| Population | max(200, min(n, 2000)) |
| Générations | 300 |
| Croisement | Uniforme, taux 0.9 |
| Mutation | Gaussienne adaptative (σ = 15% du gène), taux 0.25 |
| Élitisme | 5% |
| Sélection | Tournoi (taille 5) |

### Encodage

Chromosome = liste de `Double` (un par carré), chaque gène = surface de parc proposée en m² dans `[0, CARRE_SURFACE]`.

### Fitness

Simulation séquentielle du décodage :
1. Les gènes sont appliqués dans l'ordre des carrés triés par déficit décroissant
2. Un carré déjà couvert par propagation → gène ignoré (pénalisé dans `totalExcessPenalty`)
3. Plafonnement au déficit vers `recoSpc`
4. Propagation aux voisins
5. Résultat = `Σ (deficitBefore − deficitAfter) × pop − totalExcessPenalty × 0.05`

### Post-traitement : Sweep

Quand des gènes ont dérivé sous `minParkSurface`, des zones restent déficitaires. Un **sweep** final identique à l'itératif les couvre systématiquement.

### Initialisation

Les gènes initiaux sont initialisés à `(reco − spc) × pop ± bruit 20%` pour les carrés déficitaires, 0 sinon. Donne à l'AG un point de départ proche de la solution itérative.

---

## Factory

**Fichier :** `ProposalComputationStrategyFactory.java`

```java
public static List<ProposalComputationTypeAlgo> getAvailableTypes() {
    return List.of(
        ITERATIVE_2,   // ← actif
        ITERATIVE_3,   // ← actif
        PPC_3,         // ← actif
        CHI2_6,        // ← actif
        GENETIC_7      // ← actif
        // ITERATIVE_1, PPC_1, PPC_2, CHI2_5  // ← commentés
    );
}
```

---

## Enum : `ProposalComputationTypeAlgo`

| Valeur | Display name | Classe |
|---|---|---|
| `ITERATIVE_1` | Itératif par déficit v1 | `IterativeComputationDeficit1Strategy` |
| `ITERATIVE_2` | Itératif par déficit v2 | `IterativeComputationDeficit2Strategy` |
| `ITERATIVE_3` | Itératif par population v2 | `IterativeComputationPopulation1Strategy` |
| `PPC_1` | Solveur PPC v1 | `Solver1ComputationStrategy` |
| `PPC_2` | Solveur PPC v2 | `Solver2ComputationStrategy` |
| `PPC_3` | Solveur PPC v3 | `Solver3ComputationStrategy` |
| `CHI2_5` | χ² avec voisins v1 | `LeastSquaresNeigbour1Strategy` |
| `CHI2_6` | χ² avec voisins v3 | `LeastSquaresNeighbour2Strategy` |
| `GENETIC_7` | Génétique v2 | `Genetic1Strategy` |

---

## Point d'entrée : `ServicePropositionParc.calculeProposition()`

**Fichier :** `ServicePropositionParc.java` (l.101-204)

1. Récupère `urbanDistance`, `recoSquareMeterPerCapita`, `minSquareMeterPerCapita` selon la densité de la commune
2. Charge les carrés INSEE 200m de la commune
3. Pour chaque carré : enrichit avec `InseeCarre200mComputedV2` (surface park OMS, population) et `Filosofil200m`
4. Calcule `accessingPopulation`, `accessingSurface`, `missingSurface` pour chaque carré
5. Crée/récupère `ParkProposalMeta` (année, insee, typeAlgo)
6. Appelle `ProposalComputationStrategyFactory.create(typeAlgo, MIN_PARK_SURFACE)`
7. `computation.compute(carreMap, ...)` → `List<ParkProposal>`
8. Persiste les propositions avec l'ID meta

---

## Comparaison des algorithmes actifs

| Critère | ITERATIVE_2 | ITERATIVE_3 | PPC_3 | CHI2_6 | GENETIC_7 |
|---|---|---|---|---|---|
| **Sélection** | Déficit pur | Impact humain | MWIS + CP | χ² filtre + déficit complet | GA + sweep |
| **Montant** | `(reco-spc)×pop` | `(reco-spc)×pop` | CP optimisé | `(reco-spc)×pop` | GA + plafonnement |
| **Propagation** | Voisins | Voisins | MWIS + update | Voisins (mute accessingSurface) | Voisins |
| **Boucle** | `n` itérations | `n` itérations | max 20 itérations MWIS | `n×100` itérations | 300 générations |
| **Optimum** | Glouton | Glouton | CP local (sous-ensemble) | Glouton + χ² | Global approx. |
| **Compl** | O(n²) | O(n²) | O(k×n) k≤20 | O(n²×100) | O(pop×gen×n) |
| **Dette** | Bug voisin newMissingSurface | (identique ITERATIVE_2) | Log incomplet | Aucune majeure | Sweep correctif |

---

## Classes disponibles non actives (dans `create()` mais hors `getAvailableTypes()`)

| Classe | Rôle | Raison probable inactivation |
|---|---|---|
| `IterativeComputationDeficit1Strategy` | Itératif v1 (référence) | Supersédé par v2 (log voisin différent) |
| `Solver1ComputationStrategy` | CP itératif (modèle complet) | Trop lent, supersédé par PPC_3 |
| `Solver2ComputationStrategy` | CP one-shot | Moins bon que PPC_3 |
| `LeastSquaresNeigbour1Strategy` | χ² v1 | Bug montant (sous-estimation ~38%) |

## Implémentations orphelines (implémentent l'interface mais non référencées dans la factory)

| Classe | Rôle |
|---|---|
| `Solver4ComputationStrategy` | MWIS+CP variant (supersédé par Solver3) |
| `SolverV3ComputationStrategy` | Modèle CP global de référence |
| `LeastSquaresNeighbourV2Strategy` | χ² variant (non câblé) |
| `Genetic2Strategy` | GA variant (pop=400, pas de sweep) |
| `Genetic3Strategy` | GA variant (pop=400, mutation 0.25, sweep) |
