# Plan d'optimisation : ComputeCarreService et ComputeIrisService (V+1)

## Objectif

Optimiser les services de calcul géographique (carré INSEE 200m et IRIS) en créant de nouvelles versions (V4 pour les carrés, V2 pour l'Iris) activées par un nouveau toggle. Les versions actuelles restent inchangées pour garantir la stabilité et la réversibilité. Les optimisations visent à réduire les doublons de requêtes géographiques et à batcher les accès aux données population.

---

## 1. Stratégie générale

- **Ne pas modifier les versions actuelles** (`ComputeCarreServiceV3`, `ComputeIrisServiceIris`).
- **Créer une nouvelle version optimisée** pour chaque service :
  - `ComputeCarreServiceV4` (toggle : `carre200m-impl=v4`)
  - `ComputeIrisServiceIrisV2` (toggle : `iris-impl=v2` ou équivalent)
- **Documenter** les doublons et points d'optimisation dans ce fichier.
- **Prévoir la validation croisée** (V3 vs V4, V1 vs V2) sur les mêmes entrées.

---

## 2. Doublons et surcoûts identifiés

### Pour ComputeCarreServiceV3

- `getSurface(Geometry)` : 1 requête SQL `ST_Area` par appel, répétée dans plusieurs boucles (parc, isochrone, etc.).
- `computePopAndDensityDetail` exécuté 2× (result + resultOms) : double requête spatiale et double accès population.
- N+1 sur `filosofil200mRepository.findByAnneeAndIdInspire` alors que `getAllCarreInMap` permet de batcher.
- `parkTypeService.populate` appelé sur la liste puis sur chaque parc.
- `parkJardinRepository.findById` appelé plusieurs fois pour le même parc.
- `serviceOpenData.isDistanceDense` recalculé inutilement.

### Pour ComputeIrisServiceIris

- Même logique de calcul spatiale, mêmes risques de doublons (requêtes intersection, surface, population).
- À auditer précisément lors du clonage pour V2.

---

## 3. Optimisations proposées (V4/V2)

- **Cache local** des intersections et surfaces pour éviter les recalculs.
- **Mutualisation** des calculs result/resultOms quand possible.
- **Batch** des accès population via les méthodes de repository adaptées (`getAllCarreInMap`).
- **Réduction des appels redondants** à `populate`, `findById`, `isDistanceDense` via passage de contexte ou cache local.
- **Batch SQL** pour les surfaces (précision géodésique), ou calcul JTS si acceptable.

---

## 4. Stratégie de validation

- Comparer les résultats V3 vs V4 (et V1 vs V2 pour l'Iris) sur un jeu d'entrée identique.
- Vérifier la non-régression métier (mêmes valeurs de surface, population, ratios).
- Prévoir un mode "double run" temporaire pour logs comparatifs.

---

## 5. Gestion des toggles

- Bien séparer les toggles pour chaque service (`carre200m-impl`, `iris-impl`).
- Documenter la procédure de bascule et de retour arrière.

---

## 6. Emplacement et nommage

- Ce plan est placé à la racine du projet : `OPTIMISATION_Carre_Iris.md`
- Les implémentations optimisées seront dans le même package que les versions actuelles, suffixées `V4` ou `V2`.

---

## 7. Points d'attention

- **Précision surface** : privilégier le batch SQL pour `ST_Area(geom, true)`.
- **Validation croisée** indispensable avant toute bascule en production.
- **Documentation** à maintenir à jour lors de l'implémentation.

---

*Document rédigé le 2026-05-31*
