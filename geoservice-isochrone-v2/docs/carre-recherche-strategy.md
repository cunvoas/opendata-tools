# Rétro-documentation : `CarreRechercheStrategy`

## Vue d'ensemble

Pattern **Strategy** pour la recherche des carrés INSEE 200m impactés par un projet d'aménagement. Permet de sélectionner dynamiquement l'algorithme de détermination des carrés touchés par une zone d'influence.

```
ProjectSimulatorService
  └── CarreRechercheStrategyFactory
        ├── ModeRechercheCarre.NEIGHBORS
        │     └── NeighborsCarreRechercheStrategy
        │           └── NeighborsHelper.findNeighbors()
        │
        └── ModeRechercheCarre.GEOMETRY_REDUCER_ISOCHRONE
              └── GeometryReducerIsochroneCarreRechercheStrategy
                    ├── GeometryPointReducer (convexHull + sampling)
                    ├── IsoChroneClientService (API IGN)
                    ├── ProjectSimulatorlIsochroneRepository (cache)
                    └── InseeCarre200mOnlyShapeRepository (intersection)
```

---

## Interface

**Fichier :** `src/main/java/.../service/project/CarreRechercheStrategy.java`

```java
Set<InseeCarre200mOnlyShape> findCarres(ProjectSimulator projectSimulator, Integer urbanDistance);
```

- `projectSimulator` : porteur de la géométrie du projet (`shapeArea`, `insee`, `id`)
- `urbanDistance` : distance OMS (urbaine 300m / péri-urbaine 1200m)
- retour : ensemble des carrés INSEE 200m impactés

---

## Stratégie 1 : `NeighborsCarreRechercheStrategy`

**Fichier :** `NeighborsCarreRechercheStrategy.java`

### Algorithme

1. **Charger tous les carrés INSEE de la commune** via `findCarreByInseeCode(insee, true)`
2. **Trouver les carrés intersectant la géométrie du projet** via `findCarreInMapArea(shapeArea)`
3. **Pour chaque carré du projet, trouver ses voisins** via `NeighborsHelper.findNeighbors()` :
   - distance à vol d'oiseau (`DistanceHelper.crowFlyDistance`) entre centroïdes
   - seuil : `< urbanDistance + 121m` (tolérance de périmètre)
4. **Aggréger** tous les voisins dans un `HashSet`

### Complexité

- O(N × M) où N = carrés intersectant le projet, M = carrés de la commune
- pas de cache ; chaque appel recalcule les voisinages

### Usage

Propre, sans dépendance externe. Ne fait que de la géométrie en mémoire. Rapide mais moins précis que la stratégie isochrone.

---

## Stratégie 2 : `GeometryReducerIsochroneCarreRechercheStrategy`

**Fichier :** `GeometryReducerIsochroneCarreRechercheStrategy.java`

### Algorithme

1. **Réduire la géométrie** via `GeometryPointReducer` :
   - `reduceConvexHullToMax10Min6(shapeArea)` → enveloppe convexe + échantillonnage uniforme (max 10 pts, min 6)
   - `getPoints(reducedGeom)` → liste des points distincts
2. **Charger les isochrones en cache** depuis `ProjectSimulatorlIsochroneRepository` (recherche binaire par `idProjectSimulator` + `point`)
3. **Pour chaque point réduit** :
   - si un isochrone existe en cache → le réutiliser
   - sinon → appeler l'API IGN (`IsoChroneClientService.getIsoChrone()`), parser le résultat, mapper vers `Geometry`
   - sleep aléatoire 80-130ms entre chaque appel IGN (rate limiting)
4. **Fusionner** tous les isochrones (`merged = merged.union(iso)`)
5. **Mettre à jour** `projectSimulator.setInfluenceArea(merged)`
6. **Nettoyer** les orphelins et persister les nouveaux isochrones
7. **Intersecter** la zone fusionnée avec les carrés INSEE via `findCarreInMapArea(merged, true)`

### Dépendances externes

- **API IGN Isochrone** : service REST externe, latence réseau, rate limiting implicite
- Problème : `Thread.sleep()` est un rate limiting fragile (préférer un scheduler / retry avec backoff)

### Cache

Les `ProjectSimulatorIsochone` sont persistés en base (via `projectSimulatorlIsochroneRepository`). La recherche utilise `Collections.binarySearch` avec un comparateur triant par `idProjectSimulator` puis `point`.

### Anomalies / Points d'attention

- **L.107** : `isochrones.removeAll(isochronesNew)` → `removeAll` sur `List` appelé entre objets identifiés par `equals()` ; si `ProjectSimulatorIsochone` ne redéfinit pas `equals()`, c'est une **référence** qui est comparée, pas l'égalité structurelle. Risque d'orphelins non nettoyés.
- **L.112** : `TODO` non résolu : "Appeler le service isochrone IGN pour chaque point" → déjà fait, le TODO est obsolète.
- **L.121** : `log.warn("getIsoChrone {}")` → message de log incomplet (pas de paramètre injecté).

---

## Factory

**Fichier :** `CarreRechercheStrategyFactory.java`

Injection Spring des deux implémentations, dispatch par `ModeRechercheCarre` :

| Mode | Stratégie |
|---|---|
| `NEIGHBORS` | `NeighborsCarreRechercheStrategy` |
| `GEOMETRY_REDUCER_ISOCHRONE` | `GeometryReducerIsochroneCarreRechercheStrategy` |

---

## Point d'entrée : `ProjectSimulatorService.populate()`

**Fichier :** `ProjectSimulatorService.java` (l.252-312)

La méthode `populate()` est le consumer unique :

```java
public Map<String, ProjectSimulatorWork> populate(..., ModeRechercheCarre mode) {
    CarreRechercheStrategy strategy = carreRechercheStrategyFactory.getStrategy(mode);
    Set<InseeCarre200mOnlyShape> carreForSimulation = strategy.findCarres(projectSimulator, urbanDistance);
    // ... enrichit chaque carré avec données Filosofil/OMS
}
```

**État actuel (l.255-256) :**
```java
// FIXME CHOOSE BEST ENGINE
// return populate(projectSimulator, urbanDistance, recoSquareMeterPerCapita, ModeRechercheCarre.NEIGHBORS);
return populate(projectSimulator, urbanDistance, recoSquareMeterPerCapita, ModeRechercheCarre.GEOMETRY_REDUCER_ISOCHRONE);
```

Le mode `GEOMETRY_REDUCER_ISOCHRONE` est actuellement actif. Le mode `NEIGHBORS` est commenté avec un FIXME indiquant que le choix du moteur n'est pas encore tranché.

---

## Helpers

### `NeighborsHelper`

**Fichier :** `NeighborsHelper.java`

- Méthode unique statique `findNeighbors(InseeCarre200mOnlyShape, List<InseeCarre200mOnlyShape>, Integer)`
- distance euclidienne entre centroïdes via `DistanceHelper.crowFlyDistance`
- seuil = `urbanDistance + 121` (121m = tolérance demi-périmètre d'un carré 200m)

### `GeometryPointReducer`

**Fichier :** `GeometryPointReducer.java`

- `reduceByConvexHull(Geometry)` → JTS `ConvexHull`
- `reduceConvexHullToMax10Min6(Geometry)` → convex hull + sampling uniforme borné [6, 10] points
- `getPoints(Geometry)` → extraction des points sans doublons de coordonnées

---

## Comparaison des stratégies

| Critère | NEIGHBORS | GEOMETRY_REDUCER_ISOCHRONE |
|---|---|---|
| Précision | Moyenne (distance vol d'oiseau) | Élevée (isochrone routier IGN) |
| Performance | Rapide (mémoire seule) | Lent (appels réseau IGN) |
| Dépendance externe | Aucune | API IGN Isochrone |
| Cache | Non | Base de données |
| Complexité code | 41 lignes | 162 lignes |
| Résilience | Haute | Faible (échec IGN → ExceptionExtract) |
| Idéal pour | Prototypage, estimation rapide | Production, résultat précis |

---

## Anti-patrons / Dette technique

1. **Choix du moteur non résolu** : `FIXME CHOOSE BEST ENGINE` dans `ProjectSimulatorService.populate()` — les deux stratégies coexistent mais une seule est active.
2. **`removeAll` sur liste sans `equals()`** : risque de fuite d'orphelins dans `GeometryReducerIsochroneCarreRechercheStrategy` (l.107).
3. **Rate limiting primitif** : `Thread.sleep()` aléatoire au lieu d'un mécanisme dédié (retry, backoff, circuit breaker).
4. **TODO obsolète** : commentaire l.112 "TODO: Appeler le service isochrone IGN pour chaque point" déjà implémenté.
5. **Log incomplet** : `log.warn("getIsoChrone {}")` sans paramètre injecté (l.121).

---

## Évolution possible

- Rendre `urbanDistance + 121` configurable (propriété applicative)
- Extraire le rate limiting dans un wrapper générique pour `IsoChroneClientService`
- Implémenter `equals()` / `hashCode()` sur `ProjectSimulatorIsochone`
- Ajouter une 3e stratégie : `OSM_ISOCHRONE` utilisant OpenRouteService ou GraphHopper
- Benchmark pour trancher le FIXME et activer le meilleur moteur par défaut
