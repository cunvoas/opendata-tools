# Tests du Service de Proposition de Parcs

## Vue d'ensemble

Ce package contient les tests unitaires pour le service `ServicePropositionParc`, qui calcule les propositions d'augmentation de surface de parc par carré INSEE de 200m x 200m.

Le service propose **deux approches complémentaires** pour résoudre le problème d'optimisation de l'allocation des parcs :
1. **Approche itérative** (`calculeEtapeProposition`) : traite les carrés un par un, par ordre de déficit décroissant
2. **Approche globale** (`calculePropositionSolver`) : utilise Choco Solver pour optimiser l'allocation sur l'ensemble de la ville

## Classe de test : ServicePropositionParcTest

### Méthode testée : `calculeEtapeProposition`

Cette méthode implémente un algorithme itératif qui traite le carré avec le plus grand déficit à chaque itération.

**Signature :**
```java
public ParkProposal calculeEtapeProposition(
    Map<String, ParkProposalWork> carreMap,
    Double minSquareMeterPerCapita,
    Double recoSquareMeterPerCapita,
    Integer urbanDistance
)
```

**Algorithme :**
1. Trier les carrés par déficit décroissant (`missingSurface`)
2. Sélectionner le carré avec le plus grand déficit
3. Si `surfacePerCapita > minSquareMeterPerCapita` → arrêt (tous les carrés sont suffisamment dotés)
4. Calculer : `surfaceÀAjouter = min(max(recoSquareMeterPerCapita - surfacePerCapita, 0), 40000) × accessingPopulation`
5. Si `surfaceÀAjouter ≥ 1000 m²` :
   - Créer un objet `ParkProposal` avec la surface proposée
   - Mettre à jour `newSurface`, `newMissingSurface` et `surfacePerCapita` du carré
   - Identifier les voisins dans le rayon `urbanDistance` (+100m de marge)
   - Mettre à jour `newSurface`, `newSurfacePerCapita` et `newMissingSurface` de chaque voisin
6. Sinon : ne rien faire (pas d'objet `ParkProposal` créé)

**Retour :**
- `ParkProposal` si un parc ≥ 1000 m² est proposé
- `null` sinon

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
- `newSurface` reste `null`
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

Crée des instances de `ParkProposalWork` pour les tests avec les paramètres :
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
- **Choco Solver** : solveur de contraintes pour l'optimisation globale

## Points d'attention

1. **Précision géographique :** Les distances sont calculées à vol d'oiseau avec `DistanceHelper`
2. **Arrondis :** Utilisation de `BigDecimal` pour les calculs financiers/surfaciques
3. **Performance :** L'algorithme est itératif, prévoir des timeouts pour les grandes communes
4. **Isolation :** Les tests sont indépendants et peuvent s'exécuter dans n'importe quel ordre

## Détails d'implémentation

### Calcul de la surface à ajouter

La formule utilisée dans `calculeEtapeProposition` est :
```java
surfaceÀAjouter = min(max(recoSquareMeterPerCapita - surfacePerCapita, 0), 40000) × accessingPopulation
```

Cette formule garantit que :
- La surface ajoutée ne dépasse pas la taille maximale d'un carré (40 000 m²)
- On ne propose pas de surface négative
- La proposition vise à atteindre la densité recommandée

### Mise à jour des voisins

Quand un parc est ajouté dans un carré :
1. Les voisins dans le rayon `urbanDistance + 100m` sont identifiés
2. Leur `newSurface` est augmentée de la surface du nouveau parc
3. Leur `newSurfacePerCapita` est recalculée : `(accessingSurface + newParkSurface) / accessingPopulation`
4. Leur `newMissingSurface` est mise à jour

### Critères d'arrêt

L'algorithme itératif s'arrête quand :
- Tous les carrés ont `surfacePerCapita > minSquareMeterPerCapita`
- OU la surface proposée pour le prochain carré serait < 1000 m²

### Optimisation avec Choco Solver

Le solver modélise le problème comme suit :
- **Variables** : une IntVar par carré représentant la surface à ajouter [0..40000]
- **Contraintes** : chaque surface = 0 OU ≥ 1000
- **Objectif** : minimiser la somme des écarts absolus entre densité réelle et densité cible
- **Voisinage** : pré-calculé pour optimiser les performances

Le solver garantit une solution optimale globale mais peut être plus lent sur de grandes communes (> 100 carrés).
