# Framing v2 — NDVI par parc : restitution (écran + carte)

**Epic :** NDVI par parc — restitution des mesures (slug : `ndvi-parc-restitution`)
**Product Owner :** cunvoas · **Trimestre cible :** T4 2026 · **Statut :** cadrage en cours
**Complémente :** [`recuperation-ndvi-parc-framing.md`](recuperation-ndvi-parc-framing.md) (v1, conservée sans modification — y décrit le WMS raster Sentinel-2, solution « D » reportée)

## SCOPE

### In scope

- S01 — Écran récapitulatif dédié `/ndvi` : table des parcs (NDVI moyen, min, max, écart-type, date d'acquisition) + une vue graphique de distribution. Le patron existant du front `/stats` (`AppStatistics.vue`, `StatsGraph.vue`, `StatsTableAccessibility.vue`) sert de socle.
- S02 — Exposition REST des mesures NDVI par parc (`GET /ndvi/summaries?annee=`) pour alimenter l'écran, et champ NDVI ajouté au GeoJSON des parcs existant (`ParkView`).
- S03 — Mode colorimétrique NDVI sur la couche Parcs **existante** (recolore les polygones par `ndvi_mean`, sur le modèle du bouton « Mode daltonien »). Pas de nouvelle couche.
- S04 — NDVI visible au clic d'un parc dans le panneau « Détail des données » existant de la carte (moyen, min, max, écart-type, date d'acquisition).
- S05 — Backend : NDVI demandé **une fois par parc et par an**, uniquement sur la ligne de l'**année de référence** (dernier élément de `application.business.insee.annees`) — au plus 791 appels/an pour la MEL au lieu de 4 appels possibles par parc aujourd'hui.

### Faits mesurés (base de dev 2026-10-26)

| Mesure | Valeur observée |
|---|---|
| Lignes `park_area_computed` | 3192 (2015:800, 2017:800, 2019:801, 2021:791) |
| Lignes avec NDVI | **230**, toutes sur **2021** |
| Fenêtre d'acquisition réelle | **2025-06-08 → 2025-06-16** (fenêtre de secours) |
| NDVI moyen global | 0.403 (min 0.018, max 0.888) |
| Répartition (dixièmes) | 0.2–0.3 : 23 · 0.3–0.4 : 93 · 0.4–0.5 : 83 · 0.5–0.6 : 24 |
| Hétérogénéité intra-parc | écart-type moyen **0.229** (Citadelle : 0.593 / σ 0.300 / 822 k m²) |
| Année de référence actuelle | 2021 (`application.business.insee.annees: 2015,2017,2019,2021`) |

Ces faits confortent deux choix du v1 : la fenêtre de secours est réellement utilisée (IS02 du v1), et la plage NDVI 0.018–0.888 est lisible par l'utilisateur.

### Out of scope (avec raison)

| # | Hors périmètre | Raison |
|---|---|---|
| OOS1 | WMS raster Sentinel-2 (solution « D ») | Déjà cadré dans le v1 ; reste valable, mais moins prioritaire tant que le récapitulatif par parc (A+B+C) n'est pas livré |
| OOS2 | Séries temporelles NDVI (historique annuel) | Une seule valeur par année de référence existe ; les séries attendent plusieurs campagnes espacées d'un an (règle de non-rappel en place) |
| OOS3 | Calcul NDVI au temps réel | consomme le quota mensuel (10 000 requêtes / 10 000 PU) et n'est pas compatible avec une campagne cadencée par parc |
| OOS4 | Édition / correction manuelle du NDVI dans l'écran | Le NDVI est une mesure satellite issue de l'API Statistical, pas une donnée saisie |
| OOS5 | Historique `ndvi_request_date` dans l'UI | Champ d'action interne (anti-rappel), pas une valeur fonctionnelle |

### Références de code exploitées

| Élément | Localisation |
|---|---|
| Entité NDVI + migrations | `ParkAreaComputed.java`, `v2.1.3/4/5_ndvi_*.sql` |
| Règle « septembre + 1 an » | `ComputeParkAreaV2.java` (`refreshNdvi`, `isNdviRequestDue`) |
| 4 calques de carte (basemaps + overlays) | front `Isochrone.vue` (OSM, WorldStreetMap + Parcs, Isochrones, Données carroyées, Cadastre) |
| Panneau « Détail des données » | front `Isochrone.vue` (contrôle `dataDetail` bottom-right) |
| Patron d'écran statistique | front `AppStatistics.vue`, `StatsGraph.vue`, `StatsTableAccessibility.vue` |

## IMPACTS

- **IM01 — Backend : une source sortante nouvelle.** Un endpoint REST NDVI + champs NDVI exposés dans le GeoJSON des parcs (`ParkView`), alimentés par `ParkAreaComputedRepository`. `raises` : IS01. `requires` : CA02.
- **IM02 — Front : une route et une page de plus.** `/ndvi` au même ensemble que `/carte`, `/stats`, `/information`. `raises` : IS02. `requires` : CA03.
- **IM03 — Front carte : deux modes supplémentaires sur le rendu existant.** Mode colorimétrique NDVI + NDVI au clic dans le panneau détail. `raises` : IS02. `requires` : CA04, CA05.
- **IM04 — Backend : la règle “un appel par parc/an” modifie la conduite de campagne.** La couverture passe progressivement de 230/791 à 791/791 avec un volume d'appels maîtrisé. `raises` : IS01. `requires` : CA01.

## ISSUES

- **IS01 — Risque de quadrupler le quota si le NDVI reste demandé par ligne (parc, annee).** Le stockage actuel est par `(id, annee)` : un parc sur 4 campagnes (2015–2021) serait appelé 4 fois par campagne annuelle. `stems from` : IM01, IM04. `requires` : D01.
- **IS02 — Rendu additionnel sans surcharger la carte (6 éléments dans le contrôleur de calques) ni dégrader l'accessibilité.** Le contrôleur agence déjà 2 fond de carte + 4 calques : un 7e layer alourdirait la lecture. `stems from` : IM02, IM03. `requires` : D02, D03, D04.
- **IS03 — Couverture partielle (230 sur 791 en 2021) : les interfaces doivent distinguer « pas encore calculé » / « NDVI présent ».** `stems from` : IM01, IM04. `requires` : D04, D05.

## DECISIONS

- **D01 — Un seul appel NDVI par parc et par an, sur l'année de référence.**
  - `addresses` : IS01 · `requires` : CA01
  - **D01.1 — Année de référence = dernier élément de `application.business.insee.annees`** (2021 aujourd'hui, en cohérence avec les 230 lignes déjà dotées d'un NDVI). Pas d'année inventée.
- **D02 — Mode colorimétrique NDVI sur la couche Parcs existante** (comme le bouton « Mode daltonien »), pas une nouvelle couche.
  - `addresses` : IS02 · `requires` : CA04
  - **D02.1 — Rampe de couleurs** : `TODO raffinement` (cohérence avec le mode daltonien à confirmer).
- **D03 — Écran récapitulatif dédié `/ndvi`** fondé sur le patron `/stats` (table, graphique, composants d'accessibilité existants).
  - `addresses` : IS02 · `requires` : CA03
  - **D03.1 — Composition exacte (tables/graphiques/« dernières acquisitions »)** : `TODO raffinement`.
- **D04 — Panneau détail enrichi au clic** : NDVI (moyen / min / max / écart-type / date d'acquisition) ou mention d'absence (D05).
  - `addresses` : IS02 · `requires` : CA05
- **D05 — Distinction explicite « pas encore calculé » / « NDVI présent »** dans les interfaces (écran, mode couleur, clic).
  - `addresses` : IS03 · `requires` : CA02, CA03, CA05

## CAPABILITIES

- **CA01 — NDVI demandé une seule fois par (parc, année de référence)** : `refreshNdvi` est restreint à la ligne de référence ; les autres années ne déclenchent pas d'appel.
  - `implements` : IM04 · `is realized by` : AV03
- **CA02 — Endpoint REST NDVI** `GET /ndvi/summaries?annee=` : par parc — id, nom, surface, NDVI moyen/min/max/écart-type, date d'acquisition, année. S'appuie sur le repository existant, sans nouvelle table.
  - `implements` : IM01 · `is realized by` : AV01
- **CA03 — Écran récapitulatif `/ndvi`** : table accessible, graphique de distribution, indication de fraîcheur (date d'acquisition la plus récente), gestion de l'absence de donnée (D05).
  - `implements` : IM02 · `is realized by` : AV01, AV02, AV04
- **CA04 — Mode colorimétrique NDVI sur la couche Parcs** : bascule on/off (comme « Mode daltonien »), recoloration selon `ndvi_mean`, couleur neutre pour les parcs sans NDVI.
  - `implements` : IM03 · `is realized by` : AV02
- **CA05 — NDVI dans le panneau détail de la carte** : valeurs affichées ou mention d'absence selon la donnée.
  - `implements` : IM03 · `is realized by` : AV01, AV02

### Reverse links

- IM01 `requires` CA02 · IM02 `requires` CA03 · IM03 `requires` CA04, CA05 · IM04 `requires` CA01
- IS01 `stems from` IM01, IM04 · IS02 `stems from` IM02, IM03 · IS03 `stems from` IM01, IM04
- IS01 `requires` D01 · IS02 `requires` D02, D03, D04 · IS03 `requires` D04, D05
- D01 `requires` CA01 · D02 `requires` CA04 · D03 `requires` CA03 · D04 `requires` CA05 · D05 `requires` CA02, CA03, CA05
- CA01 `implements` IM04 · CA02 `implements` IM01 · CA03 `implements` IM02 · CA04 `implements` IM03 · CA05 `implements` IM03
- CA01 `is realized by` AV03 · CA02 `is realized by` AV01 · CA03 `is realized by` AV01, AV02, AV04 · CA04 `is realized by` AV02 · CA05 `is realized by` AV01, AV02

## ADDED VALUES

- **AV01 — Décision factuelle, lisible et reproductible.** Le NDVI est une mesure reproductible (Rhew 2011) : sa valeur par parc (moyen, min, max, σ, date) est remise à l'utilisateur pour un aperçu informatif et vérifiable. `realizes` : CA02, CA03, CA05. `is implemented by` : US02, US03, US04, US05.
- **AV02 — Simplicité de la carte préservée.** Pas de 7e layer, pas de fenêtre nouvelle : l'information NDVI se trouve dans l'écran dédié, dans le mode colorimétrique et au clic du panneau détail. `realizes` : CA03, CA04, CA05. `is implemented by` : US03, US04.
- **AV03 — Quota CDSE respecté.** Un seul appel par parc/an sur l'année de référence : le plafond mensuel 10 000 requêtes / 10 000 PU garde une marge confortable vis-à-vis des appels réels. `realizes` : CA01. `is implemented by` : US01.
- **AV04 — Patron d'écran et accessibilité déjà en place.** L'écran `/ndvi` s'appuie sur `StatsTableAccessibility` (RGAA) déjà utilisé sur `/stats`, sans nouveau composant d'interaction à inventer. `realizes` : CA03. `is implemented by` : US02, US05.

## USER STORIES

### US01 — Un seul appel NDVI par parc et par an *(Medium — implements AV03)*

As mainteneur du projet, je veux que le backend demande le NDVI une seule fois par parc, sur la ligne de l'année de référence, afin de maîtriser strictement le quota CDSE.

```gherkin
Scenario 01.1 : campagne de mise à jour sur l'année de référence
  Given le NDVI est activé et le compte CDSE est opérationnel
  And le parc n'a jamais demandé de NDVI
  When le recalcul de la ligne de l'année de référence s'exécute après septembre et que l'appel est dû
  Then un seul appel Statistical API CDSE est tenté
  And les lignes des autres années du même parc ne sont pas appelées

Scenario 01.2 : appel sur une année non-référence est ignoré
  Given le NDVI est activé
  And le recalcul d'une ligne autre que l'année de référence s'exécute
  When le calcul s'achève
  Then aucun appel NDVI n'est tenté
  And aucun champ NDVI de cette ligne est rempli

Scenario 01.3 : l'appel du parc de référence n'est pas rejoué trop vite
  Given une demande NDVI a déjà été posée il y a moins d'un an sur le parc (année de référence)
  When le recalcul de la ligne de référence s'exécute
  Then aucun nouvel appel NDVI est tenté
  And les valeurs NDVI stockées restent inchangées

Scenario 01.4 : l'appel échoue
  Given le NDVI est activé et le recalcul de la ligne de référence s'exécute
  When l'appel CDSE échoue (timeout, 429, 5xx)
  Then les colonnes NDVI ne sont pas remises
  And un WARN est loggé
  And `ndvi_request_date` est posé ; aucun rappel avant un an
  But le calcul des indicateurs OMS se termine normalement
```

### US02 — Consulter l'écran récapitulatif `/ndvi` *(Medium — implements AV01, AV04)*

As visiteur public, je veux une page récapitulative accessible des mesures NDVI des parcs, afin de comprendre le niveau et la variabilité de la végétation communale.

```gherkin
Scenario 02.1 : affichage avec données pour l'année de référence
  Given l'année de référence possède des lignes NDVI (ex. 2021, 230 lignes)
  And la feature NDVI est activée
  When l'utilisateur accède à `/ndvi`
  Then la table liste les parcs avec moyen, min, max, écart-type, date d'acquisition
  And un graphique de distribution NDVI est visible
  And la mention de fraîcheur (date d'acquisition la plus récente) est visible

Scenario 02.2 : année sans NDVI
  Given l'année demandée ne contient aucune ligne NDVI (ex. 2015)
  When l'utilisateur accède à `/ndvi` filtrée sur cette année
  Then un message clair « NDVI non disponible pour cette année » est visible
  And aucune cellule vide ou barre à 0 n'indique un résultat trompeur
  But la consultation normale reste possible pour les années disposant d'un NDVI

Scenario 02.3 : erreur de l'API REST
  Given l'API NDVI répond en erreur ou renvoie un contenu vide
  When l'utilisateur accède à `/ndvi`
  Then un message d'erreur accessible est affiché
  And aucune donnée fallacieuse n'est montrée
```

### US03 — Colorier la couche Parcs selon le NDVI *(Low — implements AV01, AV02)*

As visiteur public, je veux activer une coloration NDVI sur la couche Parcs existante, afin de voir la végétation réelle sans ajouter d'énième couche à la carte.

```gherkin
Scenario 03.1 : activation
  Given la carte affiche la couche Parcs parmi les 4 overlays existants
  And le mode NDVI est désactivé
  When l'utilisateur active le mode NDVI
  Then la couche Parcs se recolore selon la rampe NDVI (sur `ndvi_mean`)
  And le nombre d'entrées dans le contrôleur de calques n'augmente pas

Scenario 03.2 : couverture partielle
  Given le mode NDVI est activé
  And certains parcs n'ont pas de NDVI
  Then ces parcs gardent une couleur neutre distincte
  And le reste de la carte ne s'altère pas

Scenario 03.3 : désactivation
  Given le mode NDVI est activé
  When l'utilisateur désactive le mode
  Then la couche Parcs reprend sa coloration d'origine
```

### US04 — Lire le NDVI au clic d'un parc *(Low — implements AV01, AV02)*

As visiteur public, je veux voir les statistiques NDVI d'un parc dans le panneau « Détail des données » au clic, afin d'interpréter la mesure localement sans quitter la carte.

```gherkin
Scenario 04.1 : parc avec NDVI
  Given la carte est affichée sur l'année de référence
  And le parc possède un NDVI (ex. Citadelle 0.593 / σ 0.300)
  When l'utilisateur clique sur le parc
  Then le panneau « Détail des données » affiche NDVI moyen, min, max, écart-type et date d'acquisition

Scenario 04.2 : parc sans NDVI
  Given la carte est affichée sur l'année de référence
  And le parc n'a jamais demandé son NDVI
  When l'utilisateur clique sur le parc
  Then le panneau affiche une mention « NDVI pas encore calculé »
  And aucune valeur simulée ou zéro implicite n'est montré

Scenario 04.3 : feature NDVI désactivée
  Given la feature NDVI est désactivée
  When l'utilisateur clique sur un parc
  Then aucune mention NDVI n'apparaît dans le panneau
```

### US05 — État des mesures clairement lisible *(Low — implements AV01, AV04)*

As visiteur public, je veux faire la différence entre « pas encore calculé » et « NDVI présent », afin d'éviter une lecture trompeuse du niveau de couverture.

```gherkin
Scenario 05.1 : répartition affichée sur `/ndvi`
  Given la couverture NDVI est partielle (230 sur 791 aujourd'hui pour 2021)
  When l'utilisateur consulte `/ndvi`
  Then le nombre de parcs avec NDVI et sans NDVI est affiché explicitement
  And un parc sans NDVI n'est ni masqué ni présenté comme ayant un NDVI

Scenario 05.2 : cohérence écran / carte
  Given le mode colorimétrique NDVI est actif
  And un parc n'a pas de NDVI
  When l'utilisateur clique sur ce parc coloré en neutre
  Then le panneau affiche « NDVI pas encore calculé »
  And l'expérience reste cohérente entre l'écran `/ndvi` et la carte
```

### Liens AV · US

- AV01 `is implemented by` US02, US03, US04, US05 · AV02 `is implemented by` US03, US04 · AV03 `is implemented by` US01 · AV04 `is implemented by` US02, US05

## DEPENDENCIES

### Hard

- **DH1** — Backend opérationnel avec les tables `park_area_computed` / `park_area` et la feature `application.ndvi.enabled` (activée en config). Sans cela, aucun NDVI n'est rendu.
- **DH2** — Front `geoservice-map-vue3` : ajout de la route `/ndvi`, du bouton toggable NDVI et de l'extension du panneau détail.
- **DH3** — Règle backend de référence : `application.business.insee.annees` reste la source de l'année de référence (pas de nouvelle table de config).

### Soft

- **DS1** — Arbitrage final de la rampe de couleurs NDVI (D02.1) et de son rendu en mode daltonien : recommandé avant implémentation.
- **DS2** — Arbitrage de la composition exacte de l'écran `/ndvi` (D03.1), non bloquant : la table + la distribution suffisent au premier jet.
- **DS3** — Méthodologie sur `/information` expliquant le NDVI (la recommande de `DS2` du v1 reste valable) : recommandé après implémentation.

## APPENDIX

### Faits mesurés (base de dev, 2026-10-26)

- 230 lignes `park_area_computed` sur 791 de l'année 2021 ont un NDVI : **29 %** de l'année de référence.
- Ces 230 lignes proviennent d'une seule campagne (2025-06-08 → 2025-06-16, fenêtre de secours) : les mesures sont comparables entre elles.
- Écart-type intra-parc moyen 0.229 : beaucoup de parcs sont hétérogènes — la mesure d'écart-type est aussi informative que la moyenne.

### Sources reprises du v1

1. Rhew et al. 2011 — *Validation of the Normalized Difference Vegetation Index as a Measure of Neighborhood Greenness* — https://pmc.ncbi.nlm.nih.gov/articles/PMC3225119/
2. D'Silva 2025 — *Comparative NDVI and EVI Analysis of Urban Green Infrastructure in European Cities* — https://papers.ssrn.com/sol3/papers.cfm?abstract_id=5165165379
3. NASA Earth Observatory — *Measuring Vegetation (NDVI & EVI)* — https://science.nasa.gov/earth/earth-observatory/measuring-vegetation-ndvi-evi/

### Champs NDVI disponibles (non inventés)

- `ndvi_mean`, `ndvi_min`, `ndvi_max`, `ndvi_stddev` — mesures par parc / année ;
- `ndvi_acquisition_date` — date Sentinel-2 de la fenêtre retenue ;
- `ndvi_request_date` — date de campagne (anti-rappel).

## DOCUMENT METADATA

| Champ | Valeur |
|---|---|
| Epic | NDVI par parc — restitution des mesures |
| Slug | `ndvi-parc-restitution` |
| Product Owner | cunvoas |
| Trimestre cible | T4 2026 |
| Statut | Framing v2.0 — révision après récupération réelle ; à valider par le PO |
| Date | 2026-10-26 |
| Base code | backend `geoservice-isochrone-v2` @ `feat/ndvi` · front `geoservice-map-vue3` |
| Doc complémentaire | `recuperation-ndvi-parc-framing.md` (v1, inchangé) |
| Décisions ouvertes | D02.1, D03.1 |
| Version | 2.0 |
