# Genetic1Strategy — Paramètres AG, comportement et recommandations

But
----
Ce document explique l'influence des paramètres de l'algorithme génétique implémenté dans `Genetic1Strategy.java`, identifie pourquoi les résultats de l'AG ne convergent pas vers ceux des méthodes itératives du même package, et propose des modifications concrètes (paramètres et pistes de code) pour améliorer la convergence et la comparabilité.

Checklist
---------
- [x] Expliquer le rôle de chaque paramètre AG
- [x] Détecter la cause principale du non-alignement entre AG et méthodes itératives
- [x] Proposer valeurs de paramètres à tester
- [x] Proposer modifications de l'opérateur de mutation et du seeding
- [x] Proposer options techniques pour aligner fitness et décodage
- [x] Donner commandes de build/test rapides

Paramètres AG et leur influence
-------------------------------
- POPULATION_SIZE (taille de la population)
  - Rôle : nombre d'individus évalués à chaque génération.
  - Effet : trop petit → mauvaise exploration (risque de rester dans un optimum local). Trop grand → coût CPU/mémoire élevé. Dans un espace de recherche de dimension élevée (nombre de carrés), il faut augmenter proportionnellement.

- CROSSOVER_RATE (taux de croisement)
  - Rôle : probabilité qu'une paire de parents subisse un croisement.
  - Effet : valeur typique ~0.7–0.95. Un taux très élevé favorise recombinaison intense ; trop bas, la recherche devient essentiellement par mutation.

- MUTATION_RATE (taux de mutation)
  - Rôle : probabilité qu'un chromosome soit muté à chaque génération.
  - Effet : faible → convergence prématurée, stagnation. élevé → perturbation excessive.
  - Remarque sur l'implémentation actuelle : la mutation n'altère qu'un seul gène par chromosome muté et utilise σ fixe (10% de CARRE_SURFACE). Cela peut être insuffisant pour réinjecter de la diversité dans des espaces de recherche larges.

- ELITISM_RATE (élitisme)
  - Rôle : fraction de la population conservée inchangée vers la génération suivante.
  - Effet : protège les bonnes solutions mais peut réduire la diversité si trop élevé. 5–10% est raisonnable.

- MAX_GENERATIONS (nombre de générations)
  - Rôle : durée de la recherche.
  - Effet : si trop bas, l'AG n'a pas le temps d'explorer et d'améliorer. Les méthodes itératives convergent parfois en nombre d'étapes plus élevé ; il faut augmenter ce paramètre pour laisser l'AG rivaliser.

- TOURNAMENT_ARITY (taille du tournoi)
  - Rôle : paramètres de sélection. Taille plus grande = pression de sélection plus forte.
  - Effet : si trop élevée → perte de diversité; si trop faible → lenteur de convergence.

Cause principale du non-alignement AG vs méthodes itératives
------------------------------------------------------------
L'incompatibilité vient d'une différence sémantique entre la fonction de fitness et la façon dont le chromosome est décodé/transformé en propositions :

- `ParkChromosome.fitness()` calcule la réduction du déficit en se basant sur des snapshots (initAccessingSurface, initSurfacePerCapita) et en ajoutant *simultanément* la contribution des gènes du chromosome (y compris les voisins via une matrice `neighbors`). C'est une simulation parallèle/instantanée.

- `decodeChromosome(...)` applique les gènes *séquentiellement* : il parcourt la liste des carrés dans l'ordre `keys` et met à jour `accessingSurface` de chaque carré et de ses voisins au fil des ajouts. Cette propagation cumulative change les décisions suivantes (certains carrés peuvent devenir non-déficiataires après propagation et être ignorés).

Résultat : le chromosome élu comme "meilleur" par la fitness ne produit pas nécessairement le même ensemble de propositions quand il est décodé de façon séquentielle.

Solutions techniques (prioritaires)
-----------------------------------
1) Aligner la fitness sur le décodage (recommandé)
   - Faire que `fitness()` simule exactement la même procédure que `decodeChromosome`: parcourir les carrés dans le même ordre que `keys`, appliquer chaque gène uniquement si le gène >= `minParkSurface` et si, au moment de l'application, la densité effective est < `minSpc`, puis propager l'effet aux voisins (en opérant sur une copie des arrays `accessingSurface` et `surfacePerCapita`).
   - Avantage : le score évalué est fidèle au résultat réel du décodage; l'AG optimisera la même métrique que celle utilisée pour produire des propositions.

   Pseudo-code succinct pour fitness séquentielle :

   - copier initAccessingSurface[] dans simulatedAccess[]
   - pour i in 0..n-1 :
       - if genes[i] < minParkSurface continue
       - if simulatedAccess[i] / pop[i] >= minSpc continue
       - simulatedAccess[i] += genes[i]
       - for j in neighbors[i] : simulatedAccess[j] += genes[i]
   - calculer la réduction totale du déficit en comparant initSurfacePerCapita[] et simulatedAccess[]/pop[]

2) Rendre le décodage stateless (moins recommandé)
   - Faire en sorte que `decodeChromosome` n'utilise pas la propagation cumulative mais évalue chaque carré indépendamment comme fait par la fitness actuelle. Cela peut produire une stratégie différente de celle voulue initialement par l'algorithme itératif.

3) Autre approche : changer l'encodage du chromosome
   - Par exemple utiliser une représentation ordonnée (liste d'indices + surfaces) pour expliciter l'ordre d'application et permettre à la fitness de simuler cet ordre plus naturellement.

Recommandations de paramètres à tester
--------------------------------------
- Valeurs proposées initiales :
  - POPULATION_SIZE = max(100, n * 3) — ex. 300
  - MAX_GENERATIONS = 500..2000 — ex. 1000
  - MUTATION_RATE = 0.10..0.15
  - TOURNAMENT_ARITY = 3..7 — ex. 5
  - ELITISM_RATE = 0.05..0.10

Améliorations de la mutation et du seeding
-----------------------------------------
- Mutation : au lieu de muter 1 gène fixe, appliquer la mutation sur k gènes (k aléatoire petit, p.ex. 1..max(1,n/100)) ou appliquer une mutation par-gène avec petite probabilité (p.ex. 0.01) et sigma proportionnel au besoin local (par ex σ = baseNeeded * 0.2).

- Initialisation (seeding) : injecter des solutions heuristiques dans la population initiale :
  - solutions issues de `IterativeComputationDeficit1Strategy` (convertir sa proposition en chromosome) ;
  - chromosomes « voisins » (variantes avec +/- 20% bruit) autour de ces solutions ;
  - quelques chromosomes nuls.

Protocoles d'expérimentation
----------------------------
- Test A (baseline) : paramètres actuels, mesurer nombre de propositions et fitness.
- Test B : population accrue + mutations accrues + plus de générations.
- Test C : fitness alignée (implémentation 1) et paramètres modérés.

Commandes de build rapide
-------------------------
Dans la racine du module :

```sh
cd /work/PERSO/github/opendata-tools/geoservice-isochrone-v2
mvn -q -DskipTests package
```

Si vous avez un script, test ou main qui exécute le calcul des propositions, lancez-le ensuite pour comparer les sorties.

Conclusion courte
-----------------
Le réglage des paramètres (population, mutation, générations) aide, mais la clé est d'aligner la fonction de fitness et le décodage : aujourd'hui ils modélisent deux processus différents (parallèle vs séquentiel), ce qui explique l'absence de convergence vers les résultats des méthodes itératives. Je recommande d'abord d'implémenter la simulation séquentielle dans `fitness()` (ou, moins désirable, de rendre `decodeChromosome` stateless) puis d'ajuster les paramètres par expérimentation systématique.

Si vous voulez, je peux :
- implémenter la version alignée de `fitness()` (patch Java),
- ou modifier la mutation pour toucher plusieurs gènes,
- ou ajouter un petit bench/runner qui teste plusieurs configurations automatiquement et génère un CSV des résultats.

Indiquez quelle action suivante vous préférez et je l'implémente directement.
