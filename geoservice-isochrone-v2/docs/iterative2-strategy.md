# `IterativeComputationDeficit2Strategy` — Calcul détaillé

**Fichier :** `service/solver/compute/IterativeComputationDeficit2Strategy.java`

Stratégie itérative gloutonne : à chaque étape, traite le carreau le plus déficitaire et propage l'amélioration aux voisins.

---

## Point d'entrée : `compute()`

```java
public List<ParkProposal> compute(Map<String, ParkProposalWork> carreMap,
                                  Double minSquareMeterPerCapita,
                                  Double recoSquareMeterPerCapita,
                                  Integer urbanDistance)
```

Boucle `for (i = 0; i < carreMap.size(); i++)` :
- Appelle `calculeEtapeProposition(...)` à chaque itération
- Accumule les propositions non-nulles dans `List<ParkProposal>`
- Complexité : O(n²) dans le pire cas (n = nombre de carreaux)

---

## Noyau : `calculeEtapeProposition()`

### Étape 1 — Tri par déficit

```java
List<ParkProposalWork> sorted = sortProposalsByDeficit(carreMap);
```

Délègue à `ProposalSortStrategyFactory.create(Type.DEFICIT)` qui trie les carreaux par **`newMissingSurface` décroissant** (déficit surfacique restant). Le premier élément est le carreau avec le plus grand manque de surface de parc.

Tri alternatif commenté : `sortProposalsByPersona()` (priorité populations sensibles).

### Étape 2 — Condition d'arrêt

```java
if (toProcess.getSurfacePerCapita().doubleValue() > minSquareMeterPerCapita)
    return null;
```

Si **le carreau le plus déficitaire** est déjà au-dessus du seuil minimal (`minSquareMeterPerCapita`, typ. 8 ou 10 m²/hab selon densité), alors **tous** les carreaux sont au-dessus → arrêt de l'algo.

### Étape 3 — Voisinage

```java
List<ParkProposalWork> neighbors = findNeighbors(toProcess.getIdInspire(), carreMap, urbanDistance);
```

Délègue à `ParkProposalHelper.findNeighbors()` : sélectionne les carreaux dont la distance entre centroïdes ≤ `urbanDistance + 100m`. Typiquement ~24 voisins en zone dense (300m), ~143 en péri-urbain (1200m).

### Étape 4 — Calcul de la surface à ajouter

```java
Double newParkSurface = Math.min(
    Math.max(
        recoSquareMeterPerCapita - toProcess.getSurfacePerCapita().doubleValue(),
        0
    ),
    AbstractComputationtrategy.CARRE_SURFACE  // 40 000 m²
) * toProcess.getAccessingPopulation().doubleValue();
```

**Formule complète :**

```
déficitPerCapita = max(recoSquareMeterPerCapita - densitéActuelle, 0)
déficitPlafonné   = min(déficitPerCapita, CARRE_SURFACE)  // 40 000 m² max
newParkSurface    = déficitPlafonné × populationAccédante
```

| Variable | Signification | Unité |
|---|---|---|
| `recoSquareMeterPerCapita` | Densité recommandée OMS (ex: 12) | m²/hab |
| `surfacePerCapita` | Densité actuelle du carreau | m²/hab |
| `accessingPopulation` | Population totale accédant à ce carreau | hab |
| `CARRE_SURFACE` | Surface max d'un carreau 200×200m = 40 000 | m² |
| `newParkSurface` | Surface de parc proposée | m² |

**Logique :**
1. Calcule l'écart entre la densité recommandée et la densité actuelle
2. Borne inférieure à 0 (pas de surface négative)
3. Borne supérieure à `CARRE_SURFACE` (un carreau ne peut pas contenir plus qu'un carreau)
4. Multiplie par la population accédante → surface totale nécessaire

### Étape 5 — Seuil de validation

```java
if (newParkSurface >= minParkSurface)  // 1 000 m² par défaut
```

Si la surface calculée est < 1 000 m² :
- Le déficit est jugé trop faible pour justifier la création d'un parc
- Aucune proposition émise (`proposalResult = null`)
- Le carreau reste déficitaire mais n'est pas traité

### Étape 6 — Construction de la proposition

```java
proposalResult = new ParkProposal();
proposalResult.setParkSurface(BigDecimal.valueOf(newParkSurface));
proposalResult.setCentre(toProcess.getCentre());
proposalResult.setIsDense(toProcess.getIsDense());
```

Un objet `ParkProposal` est créé avec :
- `parkSurface` : surface calculée
- `centre` : géométrie du centroïde du carreau (point WGS84)
- `isDense` : flag dense/péri-urbain hérité du carreau

### Étape 7 — Mise à jour du carreau traité

```java
toProcess.setNewSurface(BigDecimal.valueOf(newParkSurface));
toProcess.setNewMissingSurface(toProcess.getNewMissingSurface().subtract(BigDecimal.valueOf(newParkSurface)));

Double newTotalSurface = toProcess.getAccessingSurface().doubleValue() + newParkSurface;
Double newSurfacePerCapita = newTotalSurface / toProcess.getAccessingPopulation().doubleValue();
toProcess.setSurfacePerCapita(BigDecimal.valueOf(newSurfacePerCapita));
```

| Champ | Opération | Résultat |
|---|---|---|
| `newSurface` | `= newParkSurface` | Surface de parc créée sur ce carreau (delta) |
| `newMissingSurface` | `= newMissingSurface - newParkSurface` | Déficit restant après création |
| `surfacePerCapita` | `= (accessingSurface + newParkSurface) / accessingPopulation` | Nouvelle densité après ajout |

**Note :** `newMissingSurface` n'a pas de `max(0)` ici → peut devenir négatif si la surface ajoutée dépasse le déficit du carreau central. C'est un bug probable (contrairement aux voisins qui ont `max(BigDecimal.ZERO)`).

### Étape 8 — Propagation aux voisins

```java
for (ParkProposalWork neighbor : neighbors) {
    Double neighborNewTotalSurface = neighbor.getAccessingSurface().doubleValue() + newParkSurface;

    Double neighborNewSurfacePerCapita = null;
    if (neighbor.getAccessingPopulation().doubleValue() != 0) {
        neighborNewSurfacePerCapita = neighborNewTotalSurface / neighbor.getAccessingPopulation().doubleValue();
        neighbor.setNewSurfacePerCapita(BigDecimal.valueOf(neighborNewSurfacePerCapita));
    } else {
        neighbor.setNewSurfacePerCapita(null);
    }
    neighbor.setNewSurface(new BigDecimal(String.valueOf(neighborNewTotalSurface)));
    neighbor.setNewMissingSurface(toProcess.getNewMissingSurface().subtract(BigDecimal.valueOf(newParkSurface)).max(BigDecimal.ZERO));
}
```

Pour chaque voisin dans le rayon d'accessibilité :

| Champ | Opération | Résultat |
|---|---|---|
| `newSurface` | `= accessingSurface + newParkSurface` | Nouvelle surface totale accessible (valeur absolue cumulée, PAS un delta — incohérent avec le carreau central qui stocke un delta) |
| `newSurfacePerCapita` | `= newSurface / population` ou `null` si pop=0 | Nouvelle densité du voisin |
| `newMissingSurface` | `= max(toProcess.newMissingSurface - newParkSurface, 0)` | Déficit restant **basé sur le carreau central**, pas sur le déficit propre du voisin |

**Bug identifié** (ligne 183) : `newMissingSurface` du voisin est calculé depuis `toProcess.getNewMissingSurface()` (celui du carreau central après soustraction), pas depuis son propre `newMissingSurface`. Cela mutualise artificiellement le déficit résiduel dans tout le voisinage, ce qui est incorrect — chaque voisin a son propre déficit qui devrait être réduit indépendamment.

---

## Diagramme de flot

```
compute()
│
├── i=0 : calculeEtapeProposition()
│     ├── sortProposalsByDeficit(carreMap)
│     ├── toProcess = sorted.get(0)          ← carreau + déficitaire
│     ├── if spc > minSeuil → return null     ← condition d'arrêt
│     ├── neighbors = findNeighbors(...)       ← voisins dans le rayon
│     ├── newParkSurface = (reco - spc) × pop  ← surface à ajouter
│     ├── if newParkSurface < minParkSurface → return null  ← seuil
│     ├── ParkProposal ← creation
│     ├── update(toProcess)                    ← newSurface, spc, missing
│     └── for neighbor in neighbors:
│           update(neighbor)                   ← newSurface, spc, missing (bug)
│     └→ return proposal
│
├── i=1 : calculeEtapeProposition()           ← carreMap modifié
│     └→ ...
│
└── i=n : calculeEtapeProposition()
      └→ return null (tout le monde ≥ minSeuil)
```

---

## Constantes utilisées

| Constante | Valeur | Source |
|---|---|---|
| `CARRE_SURFACE` | 40 000 m² | `AbstractComputationtrategy` |
| `minParkSurface` | 1 000 m² | Paramètre constructeur (vient de `AT_LEAST_PARK_SURFACE`) |
| `minSquareMeterPerCapita` | 8 (dense) / 10 (péri-urbain) | Paramètre appelant |
| `recoSquareMeterPerCapita` | 12 m²/hab (OMS) | Paramètre appelant |
| `urbanDistance` | 300 m (dense) / 1200 m (péri-urbain) | Paramètre appelant |

---

## Modèle de données : `ParkProposalWork`

Champs pertinents utilisés dans le calcul :

| Champ | Type | Rôle |
|---|---|---|
| `idInspire` | `String` | Identifiant unique du carreau |
| `annee` | `Integer` | Année de recensement |
| `centre` | `Point` | Centroïde WGS84 |
| `isDense` | `Boolean` | True si zone urbaine dense |
| `accessingPopulation` | `BigDecimal` | Population totale accédant à ce carreau |
| `accessingSurface` | `BigDecimal` | Surface de parc totale accessible depuis ce carreau |
| `surfacePerCapita` | `BigDecimal` | `accessingSurface / accessingPopulation` |
| `newMissingSurface` | `BigDecimal` | Déficit surfacique restant |
| `newSurface` | `BigDecimal` | Surface de parc proposée/ajoutée (delta ou cumul selon contexte) |
| `newSurfacePerCapita` | `BigDecimal` | Nouvelle densité après calcul |

---

## Bugs et incohérences

| N° | Description | Ligne | Impact |
|---|---|---|---|
| 1 | `newMissingSurface` du voisin basé sur `toProcess.getNewMissingSurface()` au lieu de sa propre valeur | 183 | Mutualisation incorrecte du déficit dans le voisinage |
| 2 | `neighbor.setNewSurface(...)` stocke une valeur absolue cumulée (`neighborNewTotalSurface`) alors que `toProcess.setNewSurface(...)` stocke un delta (`newParkSurface`) | 168, 155 | Incohérence sémantique du champ `newSurface` (delta vs cumul) |
| 3 | Pas de `max(0)` sur `newMissingSurface` du carreau central | 157 | Peut devenir négatif si `newParkSurface > newMissingSurface` |
| 4 | `log.error(...)` pour une proposition réussie | 186 | Niveau log incorrect (devrait être `info` ou `debug`) |
