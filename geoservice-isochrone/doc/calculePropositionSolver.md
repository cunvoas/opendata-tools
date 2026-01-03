# Documentation de la méthode calculePropositionSolver

## Vue d'ensemble

La méthode `calculePropositionSolver` utilise **Choco Solver**, un solveur de contraintes, pour calculer de manière optimale les propositions d'ajout de parcs sur l'ensemble d'une ville en une seule résolution.

## Principe

Contrairement à l'approche itérative de `calculeEtapeProposition` qui traite les carrés un par un, cette méthode formule le problème comme un **problème de satisfaction de contraintes (CSP)** et utilise Choco Solver pour trouver une solution globalement optimale.

## Modélisation mathématique

### Variables de décision

Pour chaque carré `i` parmi `n` carrés :
- `surfaceAAjouter[i]` : surface de parc à ajouter au carré (en m²)
- Domaine : `[0, 40000]` (40000 = 200m × 200m)

### Contraintes

#### 1. Contrainte de surface minimale
Pour chaque carré `i` :
```
surfaceAAjouter[i] = 0  OU  surfaceAAjouter[i] ≥ 1000
```
Un parc fait soit 0 m² (pas de parc), soit au moins 1000 m².

#### 2. Calcul de la surface totale accessible
Pour chaque carré `i` avec ses voisins `V(i)` :
```
surfaceTotale[i] = surfaceExistante[i] + surfaceAAjouter[i] + Σ(surfaceAAjouter[j]) pour j ∈ V(i)
```

#### 3. Calcul de l'écart à la cible
Pour chaque carré `i` :
```
surfaceCible[i] = population[i] × densitéRecommandée
écart[i] = surfaceCible[i] - surfaceTotale[i]
écartAbsolu[i] = |écart[i]|
```

### Fonction objectif

Minimiser la somme des écarts absolus :
```
MIN Σ(écartAbsolu[i]) pour i = 1..n
```

Cette fonction favorise une répartition équilibrée qui rapproche tous les carrés de leur objectif de densité.

## Algorithme détaillé

### Phase 1 : Préparation des données

```
1. Créer le modèle Choco "Optimisation Globale Parcs"
2. Indexer tous les carrés pour accès rapide
3. Pour chaque carré i :
   - Créer variable surfaceAAjouter[i] avec domaine [0, 40000]
   - Poser contrainte : surfaceAAjouter[i] = 0 OU ≥ 1000
```

### Phase 2 : Calcul des voisinages

```
4. Pour chaque carré i :
   - Identifier les voisins dans le rayon urbanDistance
   - Stocker la liste des ID voisins pour optimisation
```

### Phase 3 : Modélisation des contraintes

```
5. Pour chaque carré i :
   a. Récupérer population et surface existante
   b. Calculer surfaceCible = population × densitéRecommandée
   
   c. Créer tableau surfacesVoisins[] contenant :
      - surfaceAAjouter[i] (le carré lui-même)
      - surfaceAAjouter[j] pour chaque voisin j
   
   d. Créer variable sommeSurfacesAjoutees = Σ(surfacesVoisins)
   
   e. Créer variable surfaceTotale
      Contrainte : surfaceTotale = sommeSurfacesAjoutees + surfaceExistante
   
   f. Créer variable surfaceCibleVar = surfaceCible (constante)
   
   g. Créer variable écart[i]
      Contrainte : écart[i] = surfaceCibleVar - surfaceTotale
   
   h. Créer variable écartAbsolu[i] = |écart[i]|
```

### Phase 4 : Définition de l'objectif

```
6. Créer variable objectif = Σ(écartAbsolu[i])
7. Définir : MINIMIZE objectif
```

### Phase 5 : Résolution

```
8. Lancer le solveur
9. SI solution trouvée :
   a. Pour chaque carré i :
      - Récupérer surfaceAAjouter[i].getValue()
      - SI > 0 : affecter à localSurface du carré
   
   b. Pour chaque carré i :
      - Recalculer surfaceAccessible en ajoutant :
        * surfaceAccessible existante
        * localSurface du carré
        * localSurface de tous les voisins
      - Recalculer densité = surfaceAccessible / population
   
   c. Logger résultats : nb parcs, surface totale, écart
   
10. SINON :
    - Logger avertissement : aucune solution
```

## Complexité

### Temps de calcul
- **Pire cas** : exponentiel en fonction du nombre de carrés
- **En pratique** : Choco Solver utilise des heuristiques efficaces
- **Recommandation** : limite à ~100-200 carrés pour temps raisonnable

### Espace mémoire
- O(n²) pour stocker les voisinages
- O(n) pour les variables du modèle

## Avantages vs approche itérative

| Critère | Itérative | Solver |
|---------|-----------|--------|
| **Optimalité** | Minimum local | Optimum global |
| **Interactions** | Séquentielles | Simultanées |
| **Équilibrage** | Non garanti | Automatique |
| **Rapidité** | O(n × k) | Exponentiel |
| **Prévisibilité** | Haute | Variable |

## Exemple d'utilisation

```java
// Récupérer les données
Map<String, ParkProposal> carreMap = prepareCarreData(insee, annee);
Double recoSquareMeterPerCapita = 12.0; // OMS
Integer urbanDistance = 300; // zone urbaine dense

// Résoudre avec le solver
service.calculePropositionSolver(
    carreMap, 
    recoSquareMeterPerCapita, 
    urbanDistance
);

// Récupérer les résultats
for (ParkProposal carre : carreMap.values()) {
    if (carre.getLocalSurface() != null && 
        carre.getLocalSurface().doubleValue() > 0) {
        
        System.out.printf("Carré %s : ajouter %.0f m² de parc%n",
            carre.getIdInspire(),
            carre.getLocalSurface().doubleValue());
    }
}
```

## Limitations et considérations

### 1. Scalabilité
Pour les grandes villes (> 500 carrés), considérer :
- Découpage en secteurs
- Timeout de résolution
- Solutions approchées

### 2. Qualité de la solution
Le solver peut :
- Ne pas trouver de solution (contraintes trop strictes)
- Trouver une solution sous-optimale (timeout)
- Prendre beaucoup de temps

### 3. Paramètres à ajuster
- Timeout du solver : `model.getSolver().limitTime("60s")`
- Stratégie de recherche : `model.getSolver().setSearch(...)`
- Heuristiques : priorité aux carrés avec plus de population

## Tests unitaires

4 scénarios de test couvrent :
1. ✅ Résolution d'un problème global avec déficits variés
2. ✅ Robustesse avec map vide
3. ✅ Comportement sans déficit (pas d'ajout)
4. ✅ Optimisation tenant compte des voisinages

Voir `ServicePropositionParcTest.java` pour les détails.

## Performance attendue

### Ville moyenne (~100 carrés)
- Temps de résolution : **< 5 secondes**
- Qualité : **optimum global** probable

### Grande ville (~500 carrés)
- Temps de résolution : **30-60 secondes**
- Qualité : **bonne solution** avec timeout

### Très grande ville (~1000+ carrés)
- Recommandation : **découpage en secteurs**
- Ou utilisation de l'approche itérative

## Références

- [Choco Solver Documentation](https://choco-solver.org/)
- [CSP - Constraint Satisfaction Problem](https://en.wikipedia.org/wiki/Constraint_satisfaction_problem)
- OMS - Recommandations espaces verts urbains
