# Framing — Récupération du NDVI pour un parc

**Epic :** Récupération du NDVI pour un parc (slug : `recuperation-ndvi-parc`)
**Product Owner :** cunvoas · **Trimestre cible :** T4 2026 · **Statut :** cadrage en cours

## SCOPE

### In scope

- S01 — Ajout d'une couche raster NDVI type WMS (image translucide, PNG + alpha, opacité 0.6) superposée à la carte de consultation publique `geoservice-map-vue3` (`src/components/Isochrone.vue`).
- S02 — Bascule NDVI via `l-control-layers` existant, en overlay à côté du calque existant « Parcs et Jardins (MEL) » (nom proposé : « Végétation réelle (NDVI Satellite) »).
- S03 — Source de données : Sentinel-2 NDVI via un flux WMS (fournisseur approfondi au layer DECISIONS) ; aucune donnée satellite stockée localement, l'instance et le calque NDVI sont configurés côté front.
- S04 — Front-only : aucune modification du backend Java `geoservice-isochrone-v2`.

### Out of scope (avec raison)

| # | Hors périmètre | Raison |
|---|---|---|
| OOS1 | Calcul ou stockage NDVI dans le backend Java | Décision front-only exprimée par le PO ; évite le stockage d'imagerie lourde |
| OOS2 | Zonal statistics (NDVI moyen par parc) | Technique décrite par l'article SSRN 5165379 mais jamais demandée dans cet epic |
| OOS3 | Calque EVI | La demande porte sur le NDVI ; les articles recommandent l'EVI comme complément en zone dense — candidat future évolution |
| OOS4 | Navigation temporelle (séries historiques, choix de dates d'acquisition) | Non demandé ; la fraîcheur est bornée par la mosaïque du fournisseur |
| OOS5 | Cache local des tuiles NDVI | Le flux WMS est servi directement ; une mise en cache serait une optimisation différée |

### Références scientifiques mobilisées

1. Rhew et al. 2011 — « Validation of the Normalized Difference Vegetation Index as a Measure of Neighborhood Greenness » (PMC3225119) : le NDVI est une mesure objective reproductible de la végétation vs cotation d'experts (r ≈ 0.69).
2. D'Silva 2025 — « Comparative NDVI and EVI Analysis of Urban Green Infrastructure in European Cities » (SSRN 5165379) : NDVI + EVI sur villes européennes ; l'EVI corrige atmosphère/sol quand le NDVI seul est limité en zone dense.
3. NASA Earth Observatory — « Measuring Vegetation (NDVI & EVI) » : formule NDVI = (NIR−VIS)/(NIR+VIS), plage −1..+1, lecture des valeurs (eau nulles, sol nu ≈ 0, forêt dense 0.6–0.8), sensibilité aux nuages.

## IMPACTS

- **IM01 — Frontend carte publique modifié** : `Isochrone.vue` gagne une couche tuile supplémentaire (rendu NDVI) + paramètres WMS (endpoint, calque, opacité, attribution) dans la configuration applicative du front.
- **IM02 — Nouvelle dépendance externe en runtime** : tout visiteur public ayant activé le calque charge des tuiles depuis un fournisseur tiers gratuit (compte + quota) → disponibilité, limites de taux, crédit obligatoire « Copernicus Sentinel data » à afficher.
- **IM03 — Perception utilisateurs changée** : le visiteur voit la vérité pixel (cimes, période sèche) au lieu des seuls contours vectoriels des parcs → besoin de garde-fous de lisibilité (légende d'échelle, crédit, mention de qualité nuage).
- **IM04 — Déploiement front-only** : release front (build + variable d'env) uniquement ; aucun changement de schéma, aucun contrat d'API, aucun déploiement Java.

> Liens `raises` : IM01 → IS01 · IM02 → IS02, IS03 · IM03 → IS02. Liens `requires` (vers CAPABILITIES) ajoutés à la création de cette couche.

## ISSUES

- **IS01 — Configuration d'instance WMS absente** : le snippet PO contient `https://copernicus.eu<VOTRE_INSTANCE_ID>` et un calque `'NDVI'` générique — aucun identifiant d'instance réel ni nom de calque configuré n'existe. Sans ces valeurs, la couche ne charge aucun pixel.
  - *stems from* : IM01 · *requires* : D01
- **IS02 — Fraîcheur de l'image & nuages** : Sentinel-2 repasse tous les ~5 jours et les nuages faussent le NDVI (source NASA) ; en zone urbaine dense le NDVI seul plafonne face à l'EVI (source SSRN). Sans stratégie de mosaïque (masquage nuage + fenêtre temporelle) gérée côté fournisseur, le rendu affiche du bruit ou une image périmée.
  - *stems from* : IM02, IM03 · *requires* : D02
- **IS03 — Quota gratuit consommé par les visiteurs anonymes** : le flux est appelé depuis les navigateurs des visiteurs ; en cas de dépassement de quota du fournisseur, les tuiles échouent et peuvent laisser des carrés d'erreur visibles si l'échec n'est pas toléré. Ratification PO : couche OFF par défaut, entrée masquée si configuration absente/invalide, échec de tuiles silencieux.
  - *stems from* : IM02 · *requires* : D03
