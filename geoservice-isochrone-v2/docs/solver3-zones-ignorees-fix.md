# Solver3 — Correctif "zones entières ignorées"

## Contexte

`Solver3ComputationStrategy` utilise une décomposition MWIS (_Maximum Weight Independent Set_) itérative pour proposer des emplacements de parcs.  
Avant ce correctif, certaines zones déficitaires n'obtenaient **jamais** de proposition malgré un déficit significatif.

---

## Diagnostic : pourquoi des zones étaient ignorées

### 1. Blocage MWIS avec `urbanDistance` élevé

La contrainte d'isolation MWIS garantit qu'aucune zone sélectionnée n'est voisine d'une autre sélectionnée.  
Avec un `urbanDistance` élevé (ex. 600 m), une zone peut avoir **30 à 40 voisins**. Si tous ces voisins ont un déficit plus élevé, ils sont sélectionnés en priorité dans les itérations successives, excluant systématiquement la zone cible.

**Avec `MAX_ITER = 20` :** si une zone a plus de 20 voisins à plus fort déficit, elle est exclue pendant les 20 itérations et n'obtient jamais de parc.

### 2. Zones sans déficit propre jamais éligibles

Dans l'ancien code, seules les zones avec **déficit propre ≥ MIN_UNITS** étaient sélectionnables par MWIS.  
Or, dans SolverV3 (le solver global de référence), une zone sans déficit propre peut accueillir un parc si un **voisin** est déficitaire (`besoinZone = max(besoin propre, max(besoin voisins))`).

**Conséquence :** zone Y (sans déficit) adjacent à zone X (déficit = 5) ne recevait jamais de proposition, même si un parc en Y couvrirait X via l'effet de voisinage.

### 3. Pourquoi le correctif de formule précédent n'avait aucun impact visible

Le correctif `ceil(A−B)` vs `ceil(A)−ceil(B)` produisait des résidus de `0 → 1` pour certaines zones.  
Mais `1 < MIN_UNITS = 2` → ces zones restaient non-sélectionnables → **aucun changement de propositions**.

---

## Solution apportée

### Fix 1 — `selectMwisIsolated` : besoinZone dynamique

```java
// Avant : seules les zones avec résiduel propre >= MIN_UNITS étaient éligibles
.filter(e -> e.getValue() >= MIN_UNITS)  // uniquement déficit propre

// Après : besoinZone = max(propre, max(voisins))
// Les zones sans déficit propre adjacentes à des zones déficitaires sont désormais éligibles
Map<String, Integer> besoinZoneMap = new HashMap<>();
for (Map.Entry<String, Integer> e : residualUnitsMap.entrySet()) {
    int bz = e.getValue();
    for (ParkProposalWork v : voisinages.get(e.getKey())) {
        bz = Math.max(bz, residualUnitsMap.getOrDefault(v.getIdInspire(), 0));
    }
    besoinZoneMap.put(e.getKey(), bz);
}
```

**Effet :** zone Y (résiduel=0, voisin X avec résiduel=5) → `besoinZone[Y]=5` → Y est sélectionnable. Le parc placé en Y couvre X via `updateResidualDeficits`.

---

### Fix 2 — `solveSubset` : cible du gap = besoinZone

```java
// Avant : gap + addVar = residual
// Pour résiduel=0 → addVar forcé à 0 (bug critique !)

// Après : gap + addVar = besoinZone
int gapTarget = besoinZone;  // ≥ residual propre
IntVar gap = model.intVar("g_" + id, 0, gapTarget);
model.arithm(gap, "+", addVar, "=", gapTarget).post();
```

**Pourquoi c'était un bug :** si une zone non déficitaire (résiduel=0) était sélectionnée via `besoinZone`, la contrainte `gap + addVar = 0` forçait `addVar = 0` → le CP lui attribuait **aucun parc** malgré sa sélection.

---

### Fix 3 — `fallbackGreedy` : utilise besoinZone

Le fallback glouton (exécuté si le CP échoue) utilise désormais `besoinZoneMap` au lieu de `residualUnitsMap`, de façon cohérente avec les deux autres corrections.

---

### Fix 4 — Passe finale de finition dans `compute()`

```java
// Après MAX_ITER itérations, s'il reste des zones avec résiduel >= MIN_UNITS :
for (String id : carreIds) {
    int residual = residualUnitsMap.getOrDefault(id, 0);
    if (residual >= MIN_UNITS) {
        additionsM2.merge(id, residual * UNIT_M2, Integer::sum);
    }
}
updateResidualDeficits(...);
```

**Filet de sécurité garanti :** même si le voisinage est si dense que les Fix 1-3 ne suffisent pas en 20 itérations, toute zone avec déficit résiduel ≥ `MIN_UNITS` (1 000 m²) reçoit un parc direct.

---

## Comportement attendu après correctif

| Scénario | Avant | Après |
|---|---|---|
| Zone X bloquée 20 itérations (voisinage dense) | Aucune proposition | Parc via passe finale |
| Zone Y (non déficitaire) voisine de X (déficit=5) | Jamais sélectionnée | Sélectionnée, parc en Y couvre X |
| Zone avec besoin=1 (500 m²) sous MIN_UNITS | Ignorée (par design) | Toujours ignorée (taille minimum 1 000 m²) |
| Qualité OPTIMALE (tous déficits ≥ 1 000 m² couverts) | Possible seulement si MAX_ITER suffisant | Garantie |

---

## Impact sur les logs

```
# Cas normal (couverture complète en boucle MWIS)
INFO  Qualité OPTIMALE : tous les déficits couverts en 7 itération(s).

# Cas avec voisinage dense → passe finale activée
WARN  Qualité PARTIELLE après 20 itération(s) : 42 zones encore déficitaires.
INFO  Passe finale : 42 zones résiduelles → attribution directe.
INFO  Passe finale : couverture complète.

# Cas avec micro-déficits (< 1 000 m²) irréductibles
WARN  Après passe finale : 3 zones avec déficit < 1000m² (impossible à couvrir sans parc sous le seuil minimal).
```

---

## Fichiers modifiés

- `src/main/java/.../service/solver/compute/Solver3ComputationStrategy.java`

## Fichiers **non modifiés** (contrainte explicite)

- `SolverV3ComputationStrategy.java` — solver global de référence, **ne pas toucher**
