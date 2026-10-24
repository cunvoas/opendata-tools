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

> Traçabilité — `raises` : IM01 → IS01 · IM02 → IS02, IS03 · IM03 → IS02. `requires` : IM01 → CA01, CA02 · IM02 → CA02, CA04 · IM03 → CA03. (IM04 = impact de déploiement, sans capability dédiée.)

## ISSUES

- **IS01 — Configuration d'instance WMS absente** : le snippet PO contient `https://copernicus.eu<VOTRE_INSTANCE_ID>` et un calque `'NDVI'` générique — aucun identifiant d'instance réel ni nom de calque configuré n'existe. Sans ces valeurs, la couche ne charge aucun pixel.
  - *stems from* : IM01 · *requires* : D01
- **IS02 — Fraîcheur de l'image & nuages** : Sentinel-2 repasse tous les ~5 jours et les nuages faussent le NDVI (source NASA) ; en zone urbaine dense le NDVI seul plafonne face à l'EVI (source SSRN). Sans stratégie de mosaïque (masquage nuage + fenêtre temporelle) gérée côté fournisseur, le rendu affiche du bruit ou une image périmée.
  - *stems from* : IM02, IM03 · *requires* : D02
- **IS03 — Quota gratuit consommé par les visiteurs anonymes** : le flux est appelé depuis les navigateurs des visiteurs ; en cas de dépassement de quota du fournisseur, les tuiles échouent et peuvent laisser des carrés d'erreur visibles si l'échec n'est pas toléré. Ratification PO : couche OFF par défaut, entrée masquée si configuration absente/invalide, échec de tuiles silencieux.
  - *stems from* : IM02 · *requires* : D03

## DECISIONS

- **D01 — Fournisseur : Sentinel Hub (Copernicus Data Space Ecosystem) via WMS, compte gratuit.** Répond à la priorité « API gratuite » et délègue la gestion des pul images masquées au fournisseur.
  - *addresses* : IS01 · *requires* : CA02
  - **D01.1 — Endpoint exact** (instance id) : `TODO raffinement` — valeur non inventée.
  - **D01.2 — Nom du calque NDVI** configuré dans le dashboard du fournisseur : `TODO raffinement` (éventuellement calque pré-existant, sinon évaluation bandes B04/B08 côté dashboard).
  - **D01.3 — Procédure de création du compte / réservation d'instance gratuite** : `TODO raffinement`.
- **D02 — Mosaïque NDVI gérée côté fournisseur** (masquage nuages + fenêtre temporelle paramétrés dans le calque du dashboard, jamais calculée côté front).
  - *addresses* : IS02 · *requires* : CA01, CA03
  - **D02.1 — Fenêtre temporelle du calque** (dernière image masquée vs composé annuel) : `TODO raffinement` — arbitrage acte sur l'aspect saisonnier.
- **D03 — Résilience ratifiée par le PO** : calque NDVI OFF par défaut ; entrée masquée si configuration absente/invalide ; échec de tuiles silencieux (fond de carte et calque parcs toujours intacts).
  - *addresses* : IS03 · *requires* : CA01, CA02, CA04

> Crédit de source : la mention exacte exigée par le fournisseur (ex. « processed/modified Copernicus Sentinel data ») est à reprendre de sa documentation — valeur non inventée, à fixer au raffinement (pris en charge par CA03).

## CAPABILITIES

- **CA01 — Rendu & bascule de la couche NDVI** : tuiles WMS translucides (PNG+alpha, opacité 0.6) via composant vue-leaflet WMS, calque OFF par défaut, ajout au `l-control-layers` (overlay) au-dessus du fond de carte.
  - *implements* : IM01, D02, D03
- **CA02 — Configuration WMS par environnement** : URL/calque/opacité/attribution/état par défaut lus depuis la configuration du front (modes vite), pas de valeurs en dur ; config absente/invalide → entrée NDVI non proposée.
  - *implements* : D01, D03
- **CA03 — Lisibilité & crédit de source** : légende d'échelle NDVI (rampe statique — origine à sourcer chez le fournisseur, *needs decision*), attribution conforme à la doc du fournisseur, note de fraîcheur/qualité visible quand le calque est actif.
  - *implements* : IM03, D02
- **CA04 — Résilience d'affichage** : échec de chargement de tuiles NDVI silencieux, sans affecter le fond ni le calque parcs ; aucun crash.
  - *implements* : D03

### Reverse links
- IM01 *requires* CA01, CA02 · IM02 *requires* CA02, CA04 · IM03 *requires* CA03
- D01 *requires* CA02 · D02 *requires* CA01, CA03 · D03 *requires* CA01, CA02, CA04
- CA01 *is realized by* AV01, AV02 · CA02 *is realized by* AV03 · CA03 *is realized by* AV01, AV02 · CA04 *is realized by* AV04

## ADDED VALUES

- **AV01 — Crédibilité scientifique** : le NDVI est une mesure objective et reproductible de la végétation (Rhew et al. 2011, r ≈ 0.69 vs cotation d'experts) — la surcouche affiche de la donnée validée, pas une impression.
  - *realizes* : CA01, CA03
- **AV02 — Immersion** : pixels Sentinel-2 (~10 m) révélant cimes, prairies, zones dénudées à l'intérieur même des parcs, au-delà des contours vectoriels.
  - *realizes* : CA01, CA03
- **AV03 — Coût zéro & non-stockage** : API gratuite + tuiles servies à la volée = aucune imagerie hébergée, aucune facturation.
  - *realizes* : CA02
- **AV04 — Confiance préservée** : la consultation publique (données parcs officielles) reste fiable même si le flux satellite tombe.
  - *realizes* : CA04

## USER STORIES

- **US01 — Basculer la couche NDVI** *(Medium — implements AV01, AV02)*
  As visiteur public, je veux activer ou désactiver la couche NDVI depuis le contrôleur de calques, afin de comparer la végétation réelle et les parcs officiels.
- **US02 — Carte fiable en cas d'échec du flux** *(Low — implements AV04)*
  As visiteur public, je veux que la carte reste utilisable malgré l'échec des tuiles NDVI, afin de toujours pouvoir consulter les données parcs publiées.
- **US03 — Configurer le flux sans toucher au code** *(Low — implements AV03)*
  As mainteneur du projet, je veux paramétrer endpoint, calque, opacité et attribution via la configuration du front, afin de changer d'instance sans modification de code.
- **US04 — Interpréter le calque** *(Low — implements AV01, AV02)*
  As visiteur public, je veux lire la légende, le crédit de source et la note de fraîcheur du calque NDVI, afin d'interpréter correctement l'indice affiché.

### Scénarios Gherkin

#### US01 : basculer la couche NDVI
```gherkin
Scenario 01.1 : activation réussie du calque NDVI
  Given la carte publique de consultation est affichée avec le calque parcs actif
  And la configuration WMS NDVI est complète
  When le visiteur active la couche « Végétation réelle (NDVI Satellite) »
  Then les tuiles NDVI s'affichent au-dessus du fond de carte avec une opacité de 0.6
  And le calque parcs reste affiché sans changement
  And la mention d'attribution de la source est visible

Scenario 01.2 : calque NDVI inactif à l'ouverture
  Given la carte publique est ouverte par un visiteur
  When la carte termine son chargement initial
  Then la couche NDVI est désactivée par défaut
  And aucune requête de tuiles NDVI n'est émise tant que le calque n'est pas activé

Scenario 01.3 : désactivation propre du calque NDVI
  Given le calque NDVI est activé par le visiteur
  When le visiteur désactive la couche NDVI
  Then les tuiles NDVI sont retirées de la carte
  And le fond de carte et le calque parcs restent inchangés
```

#### US02 : carte fiable en cas d'échec du flux
```gherkin
Scenario 02.1 : échec de tuiles NDVI silencieux
  Given le calque NDVI est activé et la configuration WMS est valide
  When une ou plusieurs tuiles NDVI échouent (quota du fournisseur ou indisponibilité)
  Then l'échec est silencieux : aucun message bloquant n'apparaît
  And le fond de carte et le calque parcs restent utilisables
  But aucune donnée parcs n'est altérée ou masquée par l'erreur
```

#### US03 : configurer le flux sans toucher au code
```gherkin
Scenario 03.1 : valeurs du calque lues depuis la configuration
  Given la configuration du front définit l'URL WMS, le calque, l'opacité et l'attribution d'une instance provider
  When le front est construit puis la carte publique affichée
  Then la couche NDVI proposée dans le contrôleur de calques porte ces valeurs
  And aucune de ces valeurs n'est écrite en dur dans le code du composant

Scenario 03.2 : configuration absente ou incomplète
  Given la configuration WMS NDVI est absente ou incomplète (URL ou calque manquant)
  When la carte publique est affichée
  Then l'entrée NDVI n'apparaît pas dans le contrôleur de calques
  And aucun appel vers le fournisseur n'est lancé
  And la carte parcs fonctionne normalement
```

#### US04 : interpréter le calque
```gherkin
Scenario 04.1 : guidance visuelle quand le calque est actif
  Given le calque NDVI est activé
  Then la légende d'échelle NDVI (rampe définie avec le fournisseur) est visible
  And le crédit de source conforme à la documentation du fournisseur est affiché
  And une note de fraîcheur / qualité de l'image est visible

Scenario 04.2 : guidance visuelle masquée quand le calque est inactif
  Given le calque NDVI est désactivé ou déjà masqué faute de configuration
  Then la légende, le crédit NDVI et la note de fraîcheur ne sont pas affichés
```

### Liens AV · US
- AV01 *is implemented by* US01, US04 · AV02 *is implemented by* US01, US04 · AV03 *is implemented by* US03 · AV04 *is implemented by* US02

## DEPENDENCIES

### Hard
- **DH1** — Déploiement front `geoservice-map-vue3` (build + release) : sans lui aucune couche n'existe.
- **DH2** — Compte fournisseur créé + instance WMS active + calque NDVI configuré (valeurs D01.1/D01.2/D01.3/D02.1 À RENSEIGNER).
- **DH3** — Domaine WMS du fournisseur joignable depuis les navigateurs des visiteurs (aucun proxy backend requis).

### Soft
- **DS1** — Documentation fournisseur (mention légale d'attribution exacte + rampe de légende NDVI) : sans elle CA03 reste `needs decision`.
- **DS2** — Mise à jour de la page méthodologie (`AppMethodo.vue`) pour expliquer l'indice affiché : recommandé, non bloquant.
- **DS3** — Arbitrage saisonnier (D02.1) : non bloquant pour la release, affecte seulement la fenêtre d'images servies.

## APPENDIX

### Sources
1. Rhew et al. 2011 — *Validation of the Normalized Difference Vegetation Index as a Measure of Neighborhood Greenness* — https://pmc.ncbi.nlm.nih.gov/articles/PMC3225119/
2. D'Silva 2025 — *Comparative NDVI and EVI Analysis of Urban Green Infrastructure in European Cities: Implications for Sustainable Urban Planning* — https://papers.ssrn.com/sol3/papers.cfm?abstract_id=5165379
3. NASA Earth Observatory — *Measuring Vegetation (NDVI & EVI)* — https://science.nasa.gov/earth/earth-observatory/measuring-vegetation-ndvi-evi/

### Snippet PO (front, condensé)
- `L.tileLayer.wms("https://copernicus.eu<VOTRE_INSTANCE_ID>", { layers: 'NDVI', format: 'image/png', transparent: true, opacity: 0.6, maxZoom: 18, attribution: "© Copernicus Service" })` + overlay NDVI vs `parcsLayer` dans `L.control.layers`.
- Placeholders non résolus à la date de cadrage : instance ID, nom de calque, mention d'attribution exacte → couvert par D01/D02 + CA03 + DS1.

## DOCUMENT METADATA

| Champ | Valeur |
|---|---|
| Epic | Récupération du NDVI pour un parc |
| Slug | `recuperation-ndvi-parc` |
| Product Owner | cunvoas |
| Trimestre cible | T4 2026 |
| Statut | Framing v1.0 — toutes couches rédigées, Quality Gate passé (2026-10-24) |
| Date | 2026-10-24 |
| Base code | repo `cunvoas/opendata-tools`, branche `feat/ndvi` @ `6954211` (front `geoservice-map-vue3` inclus) · backend `geoservice-isochrone-v2` non modifié |
| Décisions ouvertes | D01.1, D01.2, D01.3, D02.1, rampe de légende (CA03) |
| Version | 1.0 |
