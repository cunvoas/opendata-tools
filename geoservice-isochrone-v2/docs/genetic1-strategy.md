# Genetic1Strategy — Algorithme génétique de proposition de parcs

## Contexte

Chaque **carré** est une maille INSEE de 200 m × 200 m (surface = 40 000 m²).  
L'objectif est de proposer des parcs dans des carrés déficitaires afin d'atteindre
le niveau recommandé `recoSpc` (m²/habitant), avec un seuil minimal acceptable `minSpc`.

---

## Encodage du chromosome

Chaque gène `i` est un `Double` représentant la **surface de parc proposée** (en m²)
pour le carré `i` de la liste triée par déficit décroissant (`newMissingSurface`).

```
chromosome = [gene_0, gene_1, ..., gene_n-1]
gene_i ∈ [0, CARRE_SURFACE]   (CARRE_SURFACE = 40 000 m²)
```

Un gène `< minParkSurface` (650 m²) est ignoré au décodage : aucun parc n'est proposé.

---

## Fonction de fitness

### Vue d'ensemble

$$
\text{fitness} = \underbrace{\sum_{i=0}^{n-1} \bigl(\delta_{\text{avant},i} - \delta_{\text{après},i}\bigr) \times \text{pop}_i}_{\text{réduction totale du déficit}} \;-\; \underbrace{\text{totalExcès} \times \lambda_{\text{sparsity}}}_{\text{pénalité de parcimonie}}
$$

avec $\lambda_{\text{sparsity}} = 0{,}05$ (`SPARSITY_FACTOR`).

### Phase 1 — Simulation séquentielle

On maintient un tableau `simulatedAccess[i]`, copie de `initAccessingSurface[i]`,
mis à jour au fil du parcours des gènes dans l'ordre de la liste triée.

Pour chaque gène `i` :

| Condition | Action |
|-----------|--------|
| `pop == 0` | `continue` |
| `gene < minParkSurface` | `continue` (pas de parc) |
| `simulatedAccess[i] / pop ≥ minSpc` | Zone déjà couverte → `totalExcès += gene` ; `continue` |
| sinon | Calculer `effectiveGene` et appliquer |

**Gène effectif :**

$$
\text{recoDeficit}_i = \max\!\bigl((\text{recoSpc} - \text{currentSpc}_i) \times \text{pop}_i,\; 0\bigr)
$$

$$
\text{effectiveGene}_i = \min\!\bigl(\text{gene}_i,\; \max(\text{recoDeficit}_i, \text{minParkSurface})\bigr)
$$

**Accumulation de l'excès :**

$$
\text{totalExcès} \mathrel{+}= \text{gene}_i - \text{effectiveGene}_i
$$

**Propagation :**

$$
\text{simulatedAccess}[i] \mathrel{+}= \text{effectiveGene}_i
\qquad \forall j \in \text{voisins}(i) : \text{simulatedAccess}[j] \mathrel{+}= \text{effectiveGene}_i
$$

### Phase 2 — Calcul de la réduction de déficit

$$
\delta_{\text{avant},i} = \max(0,\; \text{recoSpc} - \text{initSurfacePerCapita}[i])
$$

$$
\text{newSpc}_i = \frac{\text{simulatedAccess}[i]}{\text{pop}_i}
\qquad
\delta_{\text{après},i} = \max(0,\; \text{recoSpc} - \text{newSpc}_i)
$$

$$
\text{réductionTotale} = \sum_{i : \text{pop}_i > 0} \bigl(\delta_{\text{avant},i} - \delta_{\text{après},i}\bigr) \times \text{pop}_i
$$

### Retour final

$$
\boxed{\text{fitness} = \text{réductionTotale} - \text{totalExcès} \times 0{,}05}
$$

---

## Rôle de la pénalité de parcimonie

### Problème des gènes satellites

Sans pénalité, un gène pour une zone déjà couverte par **propagation** d'un parc voisin
est **neutre** vis-à-vis de la fitness (il tombe dans le `continue` de la Phase 1).
En sélection par tournoi, les chromosomes avec et sans ce gène ont la même chance
d'être sélectionnés → le gène **dérive** et reste souvent au-dessus de `minParkSurface`
→ au décodage, un parc superflu est créé.

### Effet de la pénalité

- Gène pour zone déjà couverte : `totalExcès += geneVal` (coût total)
- Gène surdimensionné (au-dessus du `recoDeficit`) : `totalExcès += (gene - effective)`

À `SPARSITY_FACTOR = 0,05`, un gène superflu de 10 000 m² coûte **500 points** de fitness.
Un chromosome qui supprime ce gène (gene → 0) gagne 500 points à déficit égal.
La sélection par tournoi favorise donc les chromosomes compacts.

---

## Paramètres de l'algorithme

| Paramètre | Valeur | Explication |
|-----------|--------|-------------|
| `populationSize` | `max(200, min(n, 2000))` | Dynamique : proportionnel au nombre de carrés |
| `CROSSOVER_RATE` | 0,9 | Croisement uniforme |
| `MUTATION_RATE` | 0,25 | Mutation gaussienne adaptative |
| `ELITISM_RATE` | 0,05 (5 %) | Meilleurs chromosomes conservés |
| `MAX_GENERATIONS` | 300 | Nombre de générations |
| `TOURNAMENT_ARITY` | 5 | Taille du tournoi de sélection |
| `SPARSITY_FACTOR` | 0,05 | Pénalité de parcimonie |

### Mutation gaussienne

$$
\sigma_i = \max\!\bigl(\text{gene}_i \times 0{,}15,\; \text{CARRE\_SURFACE} \times 0{,}001\bigr)
\quad (= \max(\text{gene}_i \times 15\%,\; 40\,\text{m}^2))
$$

$$
\text{gene}_i' = \text{clamp}\!\bigl(\text{gene}_i + \mathcal{N}(0, \sigma_i^2),\; 0,\; \text{CARRE\_SURFACE}\bigr)
$$

Le σ_min de 40 m² (contre 400 m² auparavant) empêche la **résurrection** des gènes
tombés sous `minParkSurface` : une mutation standard ne les repousse plus au-dessus du seuil.

---

## Comparaison avec la stratégie itérative

| Aspect | Itératif (`IterativeComputationDeficit1Strategy`) | Génétique (`Genetic1Strategy`) |
|--------|--------------------------------------------------|-------------------------------|
| Ordre de traitement | Trié par déficit à chaque itération | Trié par déficit une fois (ordre fixe) |
| Surface proposée | `min(max(recoSpc − spc, 0), CARRE_SURFACE) × pop` | Identique (plafonnement dans `fitness` et `decodeChromosome`) |
| Propagation | `setNewSurfacePerCapita` pour les voisins | `setSurfacePerCapita` + `setNewSurfacePerCapita` pour zone et voisins |
| Critère d'arrêt | `spc > minSpc` (strict) | Même filtre dans `decodeChromosome` |
| Balayage résiduel | N/A | Post-decode sweep sur les zones non couvertes |

---

## Réduction du gap observée

| Configuration | Écart GA vs itératif |
|---------------|----------------------|
| `POPULATION_SIZE = 400` (fixe) | ~80 % |
| `POPULATION_SIZE = n` (nb de carrés) | ~50 % |
| `POPULATION_SIZE = max(200, min(n, 2000))` + pénalité parcimonie + σ_min réduit | À mesurer |
