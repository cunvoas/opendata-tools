# Tests du Service de Proposition de Parcs

## Vue d'ensemble

Ce package contient les tests unitaires pour le service `ServicePropositionParc`, qui calcule les propositions d'augmentation de surface de parc par carré INSEE de 200m x 200m.

## Classe de test : ServicePropositionParcTest

### Méthode testée : `calculeEtapeProposition`

Cette méthode implémente un algorithme itératif pour proposer l'ajout de parcs dans les zones sous-dotées.

#### Scénarios de test

##### 1. `testCalculeEtapeProposition_avecDeficitImportant_ajouteParc`
**Objectif :** Vérifier qu'un parc est ajouté lorsqu'un carré présente un déficit important.

**Conditions :**
- Surface actuelle : 2 m²/hab
- Surface recommandée : 12 m²/hab
- Population : 1000 habitants
- Déficit : 10 000 m²

**Résultat attendu :**
- Un parc de surface ≥ 1000 m² est ajouté
- La surface par habitant augmente

##### 2. `testCalculeEtapeProposition_avecDeficitFaible_nAjoutePasDeParc`
**Objectif :** Vérifier qu'aucun parc n'est ajouté si la surface calculée est inférieure au minimum.

**Conditions :**
- Surface actuelle : 11 m²/hab
- Surface recommandée : 12 m²/hab
- Population : 100 habitants
- Surface calculée : 100 m² (< 1000 m²)

**Résultat attendu :**
- `localSurface` reste `null`
- Aucune modification des données

##### 3. `testCalculeEtapeProposition_sansDeficit_neRienFaire`
**Objectif :** Vérifier qu'aucune action n'est entreprise pour les carrés déjà bien dotés.

**Conditions :**
- Surface actuelle : 14 m²/hab (> 12 m²/hab)
- Pas de déficit

**Résultat attendu :**
- Aucune modification
- Surface par habitant inchangée

##### 4. `testCalculeEtapeProposition_metAJourVoisins`
**Objectif :** Vérifier que les voisins sont mis à jour suite à l'ajout d'un parc.

**Conditions :**
- Carré principal avec déficit
- Voisin à ~200m (< distance d'accessibilité)

**Résultat attendu :**
- Surface ajoutée au carré principal
- Surface par habitant du voisin augmentée

##### 5. `testCalculeEtapeProposition_carreMapVide`
**Objectif :** Vérifier la robustesse avec une map vide.

**Résultat attendu :**
- Aucune exception levée

### Méthodes auxiliaires testées

#### `sortProposalsByDeficit`
**Test :** `testSortProposalsByDeficit_triCorrect`

Vérifie que les propositions sont correctement triées par déficit décroissant.

#### `findNeighbors`
**Tests :**
- `testFindNeighbors_trouveVoisinsProches` : trouve les voisins dans le rayon d'accessibilité
- `testFindNeighbors_carreCentreInexistant` : gère le cas d'un carré inexistant

### Méthode testée : `calculePropositionSolver`

Cette méthode utilise Choco Solver pour résoudre le problème d'optimisation globale en une seule passe.

#### Scénarios de test

##### 1. `testCalculePropositionSolver_resoudProblemeGlobal`
**Objectif :** Vérifier que le solver trouve une solution globale optimale.

**Conditions :**
- 3 carrés avec déficits variés (important, moyen, faible)
- Certains carrés sont voisins, d'autres isolés

**Résultat attendu :**
- Au moins un parc ajouté
- Tous les parcs respectent la contrainte ≥ 1000 m²
- Les densités sont recalculées

##### 2. `testCalculePropositionSolver_avecCarreMapVide`
**Objectif :** Vérifier la robustesse avec une map vide.

**Résultat attendu :**
- Aucune exception levée
- Traitement gracieux du cas limite

##### 3. `testCalculePropositionSolver_carresSansDéficit`
**Objectif :** Vérifier qu'aucun parc n'est ajouté si tous les carrés sont bien dotés.

**Conditions :**
- Tous les carrés ont une densité ≥ 12 m²/hab

**Résultat attendu :**
- Aucun parc ajouté
- Pas de modification des données

##### 4. `testCalculePropositionSolver_optimiseVoisinage`
**Objectif :** Vérifier que le solver optimise en tenant compte des voisinages.

**Conditions :**
- 3 carrés voisins avec même déficit
- Distance d'accessibilité : 300m

**Résultat attendu :**
- Amélioration de la densité détectée
- Prise en compte des effets de bord (un parc bénéficie à plusieurs carrés)

## Comparaison des approches

### `calculeEtapeProposition` (Itérative)
**Avantages :**
- Rapide pour traiter un petit nombre de carrés
- Contrôle fin sur l'ordre de traitement
- Résultats prévisibles

**Inconvénients :**
- Risque de minimum local
- Nécessite plusieurs itérations
- Pas de garantie d'optimalité globale

### `calculePropositionSolver` (Globale avec Choco)
**Avantages :**
- Solution globalement optimale
- Prise en compte simultanée de toutes les interactions
- Équilibrage automatique

**Inconvénients :**
- Plus lent pour un grand nombre de carrés
- Complexité algorithmique plus élevée
- Temps de résolution non garanti

## Données de test

### Méthode utilitaire : `createParkProposal`

Crée des instances de `ParkProposal` pour les tests avec les paramètres :
- `idInspire` : identifiant unique du carré
- `longitude` / `latitude` : coordonnées géographiques
- `surfacePerCapita` : surface actuelle par habitant (m²/hab)
- `missingSurface` : surface manquante pour atteindre l'objectif (m²)
- `accessingPopulation` : population ayant accès aux parcs
- `accessingSurface` : surface totale accessible

## Exécution des tests

```bash
mvn test -Dtest=ServicePropositionParcTest
```

### Test spécifique

```bash
mvn test -Dtest=ServicePropositionParcTest#testCalculeEtapeProposition_avecDeficitImportant_ajouteParc
```

## Contraintes métier

### Constantes
- **Surface minimale d'un parc :** 1000 m²
- **Taille d'un carré INSEE :** 200m x 200m (40 000 m²)

### Recommandations OMS
- **Zone urbaine dense :**
  - Distance d'accessibilité : 300m
  - Surface recommandée : 12 m²/hab
  - Surface minimale : 8 m²/hab

- **Zone péri-urbaine :**
  - Distance d'accessibilité : 1000m
  - Surface recommandée : 12 m²/hab
  - Surface minimale : 10 m²/hab

## Dépendances

- **JUnit 5** : framework de test
- **Mockito** : mocking des dépendances
- **JTS (Java Topology Suite)** : manipulation des géométries

## Points d'attention

1. **Précision géographique :** Les distances sont calculées à vol d'oiseau avec `DistanceHelper`
2. **Arrondis :** Utilisation de `BigDecimal` pour les calculs financiers/surfaciques
3. **Performance :** L'algorithme est itératif, prévoir des timeouts pour les grandes communes
4. **Isolation :** Les tests sont indépendants et peuvent s'exécuter dans n'importe quel ordre
