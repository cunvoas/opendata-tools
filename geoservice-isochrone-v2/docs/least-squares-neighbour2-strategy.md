# LeastSquaresNeighbour2Strategy — Documentation technique

## Identité

| Attribut | Valeur |
|----------|--------|
| Classe | `LeastSquaresNeighbour2Strategy` |
| Package | `com.github.cunvoas.geoserviceisochrone.service.solver.compute` |
| Interface implémentée | `ProposalComputationStrategy` |
| Classe mère | `AbstractComputationtrategy` |
| Bibliothèque externe | Apache Commons Math3 — `PolynomialCurveFitter`, `WeightedObservedPoints` |

---

## Objectif

Produire des **préconisations de création de parcs** dans les zones déficitaires en espaces verts, en traitant les carreaux INSEE 200 m × 200 m (grille Inspire) de façon itérative du plus déficitaire au moins déficitaire.

Chaque préconisation représente la **surface totale** d'un parc à créer à l'emplacement d'un carreau pour en éliminer intégralement le déficit.

---

## Rôle du chi² (moindres carrés pondérés)

Le chi² est utilisé comme **filtre de convergence**, pas comme pilote du montant.

| Usage | Détail |
|-------|--------|
| **Filtre** (étape 1-2) | Détecter quand il ne reste plus de carreaux déficitaires → arrêt de la boucle |
| **Log** (étape 2) | Journaliser la moyenne pondérée globale des déficits pour traçabilité |
| ~~Montant proposé~~ | ~~Plafonner la surface à ajouter à `additionPerCapita * pop`~~ — **supprimé** |

> **Pourquoi le chi² n'est plus le montant ?**
> La moyenne pondérée est toujours inférieure au déficit du pire carreau. Avec l'ancienne formule
> `min(déficit, moyenneChi²) × pop`, chaque préconisation était sous-évaluée d'environ **38 %**
> par rapport à la stratégie de référence D1. La distribution spatiale était correcte,
> mais les surfaces proposées étaient insuffisantes.

---

## Algorithme pas à pas

```
RÉPÉTER jusqu'à maxIterations = taille(carreMap) × 100 :

  ÉTAPE 1 — Construire les observations chi²
    Pour chaque carreau (pop > 0 et surfaceParHab ≤ minSeuil) :
      ajouter observation(poids=pop, valeur=max(0, reco - surfaceParHab))
    Si aucune observation → ARRÊT (convergence)

  ÉTAPE 2 — Fit chi² (degré 0 = moyenne pondérée)
    additionPerCapita = clamp(coeffs[0], 0, reco)  ← loggué, non utilisé comme montant

  ÉTAPE 3 — Sélection du carreau le plus déficitaire
    toProcess = argmax(newMissingSurface)
    Si liste vide → ARRÊT

  ÉTAPE 4 — Garde déficitaire
    Si surfacePerCapita(toProcess) > minSeuil → ARRÊT (tous traités)
    Si population(toProcess) ≤ 0 → ARRÊT

  ÉTAPE 5 — Calcul du montant
    deficitParHab = max(0, reco - surfacePerCapita)
    newParkSurface = min(deficitParHab × population, CARRE_SURFACE)   ← déficit COMPLET
    Si newParkSurface < MIN_PARK_SURFACE → ARRÊT

  ÉTAPE 6 — Application sur le carreau central
    Créer ParkProposal(idInspire, newParkSurface)
    toProcess.newSurface          ← newParkSurface
    toProcess.accessingSurface    += newParkSurface   ← accumulateur cumulatif
    toProcess.surfacePerCapita    ← accessingSurface / population
    toProcess.newMissingSurface   -= newParkSurface

  ÉTAPE 7 — Propagation aux voisins (rayon urbanDistance)
    Pour chaque voisin dans findNeighbors(idInspire, carreMap, urbanDistance) :
      voisin.accessingSurface    += newParkSurface
      voisin.newSurface          ← newParkSurface
      voisin.surfacePerCapita    ← accessingSurface / popVoisin
      voisin.newMissingSurface   = max(0, newMissingSurface - newParkSurface)
```

---

## Invariant clé : `accessingSurface`

`accessingSurface` est le **seul accumulateur de surface totale accessible** depuis un carreau.
Il intègre :
- la surface originale (parcs existants)
- tous les parcs ajoutés par cette stratégie, que le carreau soit **central** (étape 6) ou **voisin** (étape 7)

Sans cette mutation dans les deux cas, une propagation entrante vers un ancien carreau central
recalculerait la base sans sa propre contribution, faisant artificiellement baisser `surfacePerCapita`
et déclenchant un `break` prématuré.

---

## Constantes héritées

| Constante | Valeur | Description |
|-----------|--------|-------------|
| `MIN_PARK_SURFACE` | 650 m² | Surface minimale en dessous de laquelle une préconisation est ignorée |
| `CARRE_SURFACE` | 40 000 m² | Taille maximale d'un carreau INSEE (200 × 200) |

---

## Paramètres d'entrée

| Paramètre | Type | Description |
|-----------|------|-------------|
| `carreMap` | `Map<String, ParkProposalWork>` | Grille de carreaux indexée par `idInspire` — **modifiée in-place** |
| `minSquareMeterPerCapita` | `Double` | Seuil de déficit (m²/hab) — en dessous : carreau déficitaire |
| `recoSquareMeterPerCapita` | `Double` | Objectif OMS (ex : 12 m²/hab) |
| `urbanDistance` | `Integer` | Rayon de propagation aux voisins (mètres) |

---

## Champs mutés sur `ParkProposalWork`

| Champ | Muté sur carreau central | Muté sur voisin | Sémantique |
|-------|:---:|:---:|------------|
| `accessingSurface` | ✅ | ✅ | Total cumulatif de surface accessible (source de vérité) |
| `surfacePerCapita` | ✅ | ✅ | `accessingSurface / population` — utilisé par le filtre chi² |
| `newSurface` | ✅ | ✅ | Delta du dernier parc ajouté (affichage) |
| `newSurfacePerCapita` | — | ✅ | Après-dernier-ajout pour affichage/rapport |
| `newMissingSurface` | ✅ (sans max 0) | ✅ (avec max 0) | Clé de tri décroissant |
| `missingSurface` | ❌ | ❌ | Baseline initiale — jamais modifiée |

---

## Comparaison avec les autres stratégies

| Critère | LeastSquaresNeighbour2 | D1 (IterativeDeficit1) | D2 (IterativeDeficit2) |
|---------|------------------------|------------------------|------------------------|
| Montant par itération | Déficit complet `(reco - sfp) × pop` | Déficit complet | Déficit complet (bugs non corrigés) |
| Filtre chi² | ✅ (convergence) | ❌ | ❌ |
| Mutation `accessingSurface` central | ✅ | ✅ | ❌ (bug) |
| Mutation `accessingSurface` voisin | ✅ | ✅ | ❌ (bug) |
| `newMissingSurface` voisin depuis bonne base | ✅ | ✅ | ❌ (double soustraction) |
| Distribution spatiale | ✅ (vérifiée) | ✅ (référence) | ⚠️ (non corrigée) |
| Écart montants vs D1 | ~0 % (après correction) | référence | non mesuré |

---

## Historique des correctifs

| Version | Correctif |
|---------|-----------|
| Bug A | `newSurface` des voisins utilisait une base incorrecte (non cumulative) |
| Bug NPE | `getAccessingPopulation()` pouvait être null → division par zéro |
| Bug C | `newMissingSurface` des voisins utilisait la valeur du carreau central (double soustraction) |
| Bug D | `surfacePerCapita` des voisins n'était pas mis à jour → le filtre chi² ne les excluait pas |
| Régression | `accessingSurface` du carreau **central** n'était pas muté → `surfacePerCapita` pouvait baisser en itérations futures |
| **Sous-évaluation** | Montant cappé à `min(déficit, moyenneChi²)` → préconisations ~38 % inférieures à D1 ; **corrigé** : déficit complet appliqué |

---

## Exemple de trace log

```
Iteration 0 : addition estimee par chi2 = 4.32 m2/hab
Iteration 0 : carreau CRS94_1000NE traite, ajout 8640.0 m2, 18 voisins mis a jour.
Iteration 1 : addition estimee par chi2 = 3.87 m2/hab
Iteration 1 : carreau CRS94_0800NE traite, ajout 6200.0 m2, 22 voisins mis a jour.
...
Iteration N : aucun deficit restant, arret.
```
