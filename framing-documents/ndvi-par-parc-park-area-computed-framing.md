# Framing — NDVI par parc — colonne ParkAreaComputed

**Epic :** NDVI par parc — colonne ParkAreaComputed (slug : `ndvi-par-parc-park-area-computed`)
**Product Owner :** cunvoas · **Trimestre cible :** T4 2026 · **Statut :** cadrage en cours

## SCOPE

### In scope

- S01 — Colonnes NDVI ajoutées à `ParkAreaComputed` (`park_area_computed`, clé `(id, annee)`) : moyenne, min, max, écart-type (BigDecimal nullable) + date d'acquisition (Date nullable).
- S02 — Acquisition : NDVI agrégé sur le polygone du parc (`ParkArea.polygon`) via l'API statistique du fournisseur satellite (fournisseur issu de l'epic 1 — Sentinel Hub).
- S03 — Intégration au pipeline existant : l'appel NDVI se déclenche pendant `ComputeParkAreaV2.computeGenericParkAreaV2()` ; la valeur est sauvegardée avec la ligne (même `save`).
- S04 — Résilience : en cas d'échec NDVI, les colonnes restent nulles et la ligne OMS est tout de même calculée et sauvegardée ; le NDVI est rattrapé au recalcul suivant de la ligne.
- S05 — Exposition : colonne seule — Aucun affichage, aucune modification des endpoints existants (lecture standard du repository).

### Out of scope (avec raison)

| # | Hors périmètre | Raison |
|---|---|---|
| OOS1 | Surcouche raster front (epic 1) | Déjà cadrée dans `recuperation-ndvi-parc-framing.md` |
| OOS2 | Calque EVI | La demande porte sur l'indice NDVI ; l'EVI reste un candidat d'évolution future |
| OOS3 | Historisation/temps ré bols NDVI indépendant du pipeline OMS | Non demandé ; le couplage de fraîcheur est tranché aux DECISIONS |
| OOS4 | Exposition front/dashboards de l'indice | Rendu expressement « colonne seule » par le PO |
| OOS5 | API satellite payante | Contrainte du PO : gratuité en priorité |
| OOS6 | Batch global dédié au rattrapage NDVI de toutes les lignes | Le rattrapage se fait ligne à ligne au recalcul, pas par un job séparé |

### Ancrage réel (vérifié)

- Entité : `geoservice-isochrone-v2/src/main/java/com/github/cunvoas/geoserviceisochrone/model/isochrone/ParkAreaComputed.java:26-64` (colonnes actuelles : surface, population, surfacePerInhabitant, populationIris, surfacePerInhabitantIris, oms, isDense, updated).
- Calcul : `service/park/ComputeParkAreaV2.java:191-280` — guard `updated` (`:78-80`), OMS type (`:90-99`), contributions carres et iris, `save` (`:249`, `:279`).
- Schéma : dev `ddl-auto: update` (`application-dev.yml:10`) → colonnes auto ; prod `ddl-auto: none` + Flyway (`application.yml:170`) → migration SQL nécessaire.
- Lecture : `ParkAreaComputedRepository` standard (`findAll`/`findByIdAndAnnee`, sans projection SQL des colonnes) + `DashboadService` insensible aux nouvelles colonnes.

## IMPACTS

- **IM01 — Schéma `park_area_computed` étendu** : colonnes NDVI (moyenne, min, max, écart-type, date d'acquisition ; nommage à aligner sur la convention existante au raffinement). Dev : auto (`ddl-auto: update`) ; prod : **migration Flyway obligatoire**.
- **IM02 — Nouvelle dépendance runtime côté backend** : le pipeline Java appelle l'API statistique du fournisseur satellite — cette fois le backend est client du service externe ; quota et erreurs HTTP deviennent un facteur du calcul OMS si non gérés.
- **IM03 — Pipeline de calcul étendu** : `ComputeParkAreaV2` gagne un appel réseau par parc recalculé (latence, echec journalizable par ligne, NDVI rattrapé au recalcul suivant) ; couplage fraîcheur NDVI ↔ recalcul OMS à arbitrer aux DECISIONS.
- **IM04 — Chemins de lecture inchangés** : repository standard + `DashboadService`/expositions reste insensibles aux nouvelles colonnes (aucun affichage — conforme au promoteur « colonne seule »).

> Traçabilité — `raises` : IM01 → IS04 · IM02 → IS01, IS03 · IM03 → IS02, IS03. `requires` : IM01 → CA01 · IM02 → CA02, CA04 · IM03 → CA03, CA04 · IM04 — sans capability (impact de lecture).

## ISSUES

- **IS01 — Point d'entrée statistique non précisé** : le fournisseur est retenu (Sentinel Hub, compte gratuit) mais aucun point d'entrée statistique par polygone ni son quota par appel n'est encore retenu. Sans cette valeur, S02 reste non implémentable.
  - *stems from* : IM02 · *requires* : D01
- **IS02 — Fraîcheur NDVI décorelée des murs OMS** : le déclenchement retenu est le recalcul OMS de la ligne ; une ligne peu recalculée peut porter un NDVI ancien. Stratégie de fraîcheur ratifiée : rafraîchissement à chaque recalcul de ligne, dérive de fraîcheur acceptsée et documentée (D02, OOS6 inchangé).
  - *stems from* : IM03 · *requires* : D02
- **IS03 — Latence / échec réseau dans le pipeline OMS** : un appel statistique par parc recalculé peut allonger le calcul ; en cas d'indisponibilité fournisseur, la ligne doit s'enregistrer sans NDVI (S04), avec budget d'appel (timeout, retry, log) à fixer.
  - *stems from* : IM02, IM03 · *requires* : D03
- **IS04 — Migration prod Flyway** : en prod `ddl-auto: none` — sans script SQL cohérent, les colonnes n'existent pas en base et l'entité cesse de mapper proprement. Script append-only à produire et appliquer.
  - *stems from* : IM01 · *requires* : D04

## DECISIONS

- **D01 — Mode d'obtention : API statistique du fournisseur** — statistiques calculées côté fournisseur sur le polygone du parc (pas de raster téléchargé, pas de calcul NDVI re-implémenté côté Java).
  - *addresses* : IS01 · *requires* : CA02
  - **D01.1 — Endpoint exact** + auth : `TODO raffinement` (valeur non inventée).
  - **D01.2 — Quota par appel / limites de taux** : `TODO raffinement`.
  - **D01.3 — Convention de sortie** (moyenne, min, max, écart-type, date d'acquisition) : mapping à documenter d'après la doc fournisseur.
- **D02 — Politique de fraîcheur ratifiée** : le NDVI est récupéré au recalcul de la ligne OMS uniquement ; dérive de fraîcheur acceptée et documentée ; pas de job séparé (OOS6 inchangé).
  - *addresses* : IS02 · *requires* : CA01, CA03
- **D03 — Stratégie d'échec en pipeline** : timeout configurable ; échec → colonnes NDVI nulles + log WARN ; pas de retry inline ; exception jamais propagée au calcul OMS (S04) ; rattrapage au recalcul suivant de la ligne.
  - *addresses* : IS03 · *requires* : CA02, CA03, CA04
  - **D03.1 — Valeur du timeout** : `TODO raffinement` (config applicative).
- **D04 — Migration prod par Flyway** : script append-only versionné (colonnes NDVI) ; dev auto via `ddl-auto: update`.
  - *addresses* : IS04 · *requires* : CA01

## CAPABILITIES

- **CA01 — Persistence des colonnes NDVI** : champs nullable sur l'entité `ParkAreaComputed` (moyenne, min, max, écart-type, date d'acquisition) + propagation JPA repository ; migration Flyway prod append-only.
  - *implements* : IM01, D04
- **CA02 — Client API statistique backend** : service Java appelant l'endpoint statistique du fournisseur pour un polygone de parc, configuré (endpoint/secret — D01.1), retournant des stats typées (moyenne, min, max, écart-type, date d'acquisition).
  - *implements* : IM02, D01, D03
- **CA03 — Intégration pipeline OMS** : point d'accroche dans `ComputeParkAreaV2.computeGenericParkAreaV2()` — récupération des statistiques NDVI du parc recalculé puis remplissage des colonnes avant `save` ; rafraîchissement à chaque recalcul de ligne (D02).
  - *implements* : IM03, D02, D03
- **CA04 — Résilience d'appel** : timeout configuré (D03.1), échec → colonnes NDVI à `null` + log WARN, exception jamais propagée au calcul OMS.
  - *implements* : D03

### Reverse links
- IM01 *requires* CA01 · IM02 *requires* CA02, CA04 · IM03 *requires* CA03, CA04 (IM04 — sans capability, impact de lecture)
- D01 *requires* CA02 · D02 *requires* CA01, CA03 · D03 *requires* CA02, CA03, CA04 · D04 *requires* CA01
- CA01 *is realized by* AV02 · CA02 *is realized by* AV01, AV03 · CA03 *is realized by* AV01, AV02 · CA04 *is realized by* AV04

## ADDED VALUES

- **AV01 — Mesure objective du vivant dans le parc** : le NDVI quantifie la densité de végétation réelle (Rhew et al. 2011, r ≈ 0.69 vs cotation d'experts) — l'indicateur OMS (surface accessible) ne dit rien de la canopie : l'indice par parc ajoute cette dimension, mesurée par une source validée.
  - *realizes* : CA02, CA03
- **AV02 — Donnée au cœur du modèle** : un NDVI par (parc, année) dans `park_area_computed` = exploitable par toute analyse future sans nouveau pipeline ni re-calcul ad hoc.
  - *realizes* : CA01, CA03
- **AV03 — zéro coût / zéro stockage raster** : statistiques calculées par le fournisseur gratuit ; aucun raster hébergé, aucun calcul NDVI maison.
  - *realizes* : CA02
- **AV04 — Pipeline toujours vivant** : un échec NDVI ne met jamais en péril les indicateurs OMS (colonnes nulles, rattrapage au recalcul suivant).
  - *realizes* : CA01, CA04

## USER STORIES

> **Note de cadrage (réel relevé)** : le gardien `computeGenericHead` (`service/park/ComputeParkAreaV2.java:78-80`) court-circuite la ligne dès que `updated` est frais et les indicateurs OMS non-null, sans regarder le NDVI. L'intégration retenue force le re-calcul NDVI quand les colonnes NDVI sont `null`, sans recalculer l'OMS (voir 01.3 / CA03).

- **US01 — Indice NDVI disponible par parc/année** *(Medium — implements AV01, AV02, AV04)*
  As analyste consommateur de données, je veux les colonnes NDVI (moyenne, min, max, écart-type, date d'acquisition) peuplées sur chaque ligne de `park_area_computed`, afin d'exploiter un indicateur de végétation sans calcul ad hoc.

- **US02 — Client d'appel isolé et configurable** *(Low — implements AV03, AV04)*
  As mainteneur backend, je veux un service d'appel statistique configurable (endpoint, secret, timeout), afin de changer de réglage sans toucher au pipeline.

- **US03 — Migration Flyway prod append-only** *(Low — implements AV02)*
  As ingénieur de déploiement, je veux un script Flyway qui ajoute les colonnes NDVI en prod, afin d'évoluer le schéma sans interruption ni destruction de données.

### Scénarios Gherkin

#### US01 : remplir les colonnes NDVI
```gherkin
Scenario 01.1 : remplissage lors du recalcul OMS
  Given la configuration de l'appel statistique est complète
  And le fournisseur est joignable
  When la ligne OMS (parc, annee) est recalculée par ComputeParkAreaV2
  Then les colonnes NDVI (moyenne, min, max, écart-type, date d'acquisition) sont remplies à partir de la réponse du fournisseur
  And la ligne est sauvegardée une seule fois avec ses indicateurs OMS intacts

Scenario 01.2 : échec NDVI non bloquant
  Given le fournisseur est injoignable ou le quota est épuisé
  When la ligne OMS est recalculée
  Then les colonnes NDVI restent nulles
  And un log WARN retrace l'échec pour ce parc
  And les indicateurs OMS de la ligne sont remplis et sauvegardés normalement
  But aucune exception n'est propagée au pipeline de calcul

Scenario 01.3 : rattrapage NDVI d'une ligne au-guard frais
  Given une ligne OMS fraîche (updated > park.updated, indicateurs OMS non-null) dont les colonnes NDVI sont nulles
  When ComputeParkAreaV2 parcourt ce parc
  Then le NDVI est recalculé uniquement
  And l'éventuelle valeur NDVI nulle est remplacée par la réponse du fournisseur
  And les valeurs OMS existantes restent inchangées
```

#### US02 : appel isolé et configurable
```gherkin
Scenario 02.1 : valeurs lues depuis la configuration
  Given la configuration applicative définit endpoint, secret et timeout de l'API statistique
  When le pipeline déclenche un calcul de parc
  Then l'appel statistique utilise ces valeurs
  And aucune valeur n'est écrite en dur dans le code du service d'appel

Scenario 02.2 : budget temporel respecté en cas d'échec
  Given un timeout T est configuré pour l'appel statistique
  When l'endpoint ne répond pas dans le délai T
  Then l'appel échoue au bout de T au maximum
  And le pipeline poursuit le calcul de la ligne immédiatement après
```

#### US03 : migration Flyway prod
```gherkin
Scenario 03.1 : activation des colonnes en production
  Given le script Flyway d'ajout des colonnes NDVI est présent dans le projet
  When le déploiement en production démarre
  Then les colonnes NDVI sont ajoutées à la table avec des valeurs nulles par défaut
  And aucune donnée OMS existante n'est modifiée ou supprimée

Scenario 03.2 : cohérence des colonnes en développement
  Given la configuration de développement active ddl-auto update
  When l'application démarre en local
  Then les colonnes NDVI sont créées automatiquement avec le même nommage que le script Flyway
```

### Liens AV · US
- AV01 *is implemented by* US01 · AV02 *is implemented by* US01, US03 · AV03 *is implemented by* US02 · AV04 *is implemented by* US01, US02

## DEPENDENCIES

### Hard
- **DH1** — Migration Flyway prod écrite + appliquée (sinon colonnes absentes en prod, entity ne mappe pas proprement — D04).
- **DH2** — Instance/endpoint statistique du fournisseur disponible + secret configuré (valeurs D01.1/D01.3 — héritées de l'epic 1) ; sinon rafraîchissement NDVI impossible (colonnes restent nulles, S04 tient).
- **DH3** — Deploy backend `geoservice-isochrone-v2` (première fois où le backend gagne une dépendance externe — Epic 1 était front-only).
- **DH4** — Payload réponse du fournisseur conforme D01.3 (mêmes statistiques : moyenne, min, max, écart-type, date d'acquisition).

### Soft
- **DS1** — Doc fournisseur : quota par appel (D01.2) et limites de taux — afin de dimensionner le volume pipeline/parcs.
- **DS2** — Coiner du timeout D03.1 après mesure de latence réelle sur les parcs.
- **DS3** — Épic 1 : réutilisation du compte/fournisseur sans rétro-impact technique (aucun lien de traçabilité inter-doc, périmètres séparés).

## APPENDIX

### Sources
1. Rhew et al. 2011 — validité du NDVI comme mesure objective de végétation (r ≈ 0.69) — https://pmc.ncbi.nlm.nih.gov/articles/PMC3225119/
2. D'Silva 2025 — NDVI vs EVI en zone dense ; stats passées au filtre zonal par ville — https://papers.ssrn.com/sol3/papers.cfm?abstract_id=5165379
3. NASA Earth Observatory — Interprétation des valeurs NDVI et sensibilité aux nuages — https://science.nasa.gov/earth/earth-observatory/measuring-vegetation-ndvi-evi/

### Ancrage code (vérifié à `6954211` / branche `feat/ndvi`)
- `model/isochrone/ParkAreaComputed.java:26-64` — entité clé `(id, annee)`.
- `service/park/ComputeParkAreaV2.java:78-80, :191-280` — guard `updated`, calcul OMS, `save` (`:249`/`:279`).
- `repo/ParkAreaComputedRepository.java` — repository standard, aucune projection SQL.
- `application-dev.yml:10` (ddl-auto update) · `application.yml:170` (flyway, prod ddl none).

## DOCUMENT METADATA

| Champ | Valeur |
|---|---|
| Epic | NDVI par parc — colonne ParkAreaComputed |
| Slug | `ndvi-par-parc-park-area-computed` |
| Product Owner | cunvoas |
| Trimestre cible | T4 2026 |
| Statut | Framing v1.0 — toutes couches rédigées, Quality Gate passé (2026-10-24) |
| Date | 2026-10-24 |
| Base code | repo `cunvoas/opendata-tools`, branche `feat/ndvi` @ `6954211` · backend `geoservice-isochrone-v2` concerné |
| Décisions ouvertes | D01.1, D01.2, D01.3, D03.1 |
| Epic lié (valeur générée) | `recuperation-ndvi-parc-framing.md` (fournisseur Sentinel Hub, front overlay) |
| Version | 1.0 |
