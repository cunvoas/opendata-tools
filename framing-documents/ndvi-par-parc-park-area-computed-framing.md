# Framing — NDVI par parc — colonne ParkAreaComputed

**Epic :** NDVI par parc — colonne ParkAreaComputed (slug : `ndvi-par-parc-park-area-computed`)
**Product Owner :** cunvoas · **Trimestre cible :** T4 2026 · **Statut :** implémenté — révision v1.3

> **Révision v1.2 (2026-10-31)** — Le NDVI n'est plus appelé dans le pipeline OMS.
> `ComputeParkAreaV2` dépose une demande dans la table `ndvi_job` ; un service planifié
> (`NdviBatchJobService`) traite la file par lots avec reprise. Un seul appel Copernicus
> est fait par parc et par année de référence ; le résultat, lissé sur plusieurs années
> par le fournisseur, est recopié sur **toutes** les lignes `park_area_computed` du parc.
> Chaque réponse contenant des statistiques est aussi historisée dans `ndvi_observation`
> (période, date d'acquisition, nombre de pixels, valeurs) pour permettre une reventilation
> ultérieure et reconstituer l'histoire végétale du parc. Les sections ci-dessous intègrent
> cette architecture.
>
> **Révision v1.3 (2026-11-08)** — La file `ndvi_job` n'a plus de clé année : un job par parc.
> Clé métier `(id, id_inspire, object_type)`, clé technique `job_id`, sans NULL (`id = -1` ou
> `id_inspire = ''` selon le type). Seul `PARK_AREA_COMPUTED` est consommé. Migrations
> `v2.1.9` (force) et `v2.2.1` (clé par objet). Voir D04 et D07.

## SCOPE

### In scope

- S01 — Colonnes NDVI ajoutées à `ParkAreaComputed` (`park_area_computed`, clé `(id, annee)`) : moyenne, min, max, écart-type (BigDecimal nullable) + date d'acquisition (Date nullable).
- S02 — Acquisition : NDVI agrégé sur le polygone du parc (`ParkArea.polygon`) via l'API statistique du fournisseur satellite (fournisseur issu de l'epic 1 — Sentinel Hub).
- S03 — Intégration au pipeline existant : `ComputeParkAreaV2.computeGenericParkAreaV2()` ne fait plus l'appel NDVI ; il dépose une demande idempotente dans la table `ndvi_job` (un job par parc, sans clé année). La ligne OMS est sauvegardée avant la mise en file.
- S04 — Résilience : en cas d'échec NDVI, les colonnes restent nulles et la ligne OMS reste intacte ; le job passe `IN_ERROR` et est rejoué quotidiennement. Un job resté `IN_PROCESS` après crash/redémarrage est recyclé par une tâche exécutée toutes les 4h, avec un seuil de 8h15.
- S05 — Exposition : aucune UI ni endpoint public ; lecture standard via les repositories (`ParkAreaComputedRepository`, `NdviObservationRepository`).
- S06 — Mutualisation : un seul appel Copernicus par parc et par année de référence ; le NDVI lissé est recopié sur toutes les années du parc pour économiser le quota mensuel. Si un NDVI est déjà présent, aucun appel n'est relancé : la donnée existante est réutilisée.
- S07 — Historique : chaque réponse Copernicus contenant des statistiques est conservée dans `ndvi_observation` (parc, date d'appel, période, date d'acquisition, nombre de pixels, moyenne/min/max/écart-type). Cet historique permet de reventiler les observations a posteriori et de reconstituer l'histoire végétale du parc ; aucune UI de consultation n'est ajoutée.

### Out of scope (avec raison)

| # | Hors périmètre | Raison |
|---|---|---|
| OOS1 | Surcouche raster front (epic 1) | Déjà cadrée dans `recuperation-ndvi-parc-framing.md` |
| OOS2 | Calque EVI | La demande porte sur l'indice NDVI ; l'EVI reste un candidat d'évolution future |
| OOS3 | Reventilation automatique des observations sur les années | L'historique est conservé pour le permettre, mais la règle d'affectation reste un post-traitement hors périmètre |
| OOS4 | Exposition front/dashboards de l'indice | Rendu expressement « colonne seule » par le PO |
| OOS5 | API satellite payante | Contrainte du PO : gratuité en priorité |
| OOS6 | Appel NDVI synchrone dans le pipeline OMS | Remplacé par une file persistante pour ne pas bloquer le calcul OMS |
| OOS7 | Calcul NDVI distinct par année | L'indicateur Copernicus est lissé sur plusieurs années : un appel par parc est recopié sur toutes les années |
| OOS8 | Consommation des autres types d'objet (`CARRE_200M`, `IRIS`, `CADASTRE`, `PARC_ET_JARDIN`) | Seul `PARK_AREA_COMPUTED` est implémenté ; le schéma accepte le type, la file ne le consomme pas (shortcut dans `NdviBatchJobService`) |

### Ancrage réel (vérifié)

- Entité : `geoservice-isochrone-v2/src/main/java/com/github/cunvoas/geoserviceisochrone/model/isochrone/ParkAreaComputed.java` (colonnes OMS + NDVI, clé `(id, annee)`).
- File d'attente : `model/admin/NdviJob.java`, `repo/admin/NdviJobRepository.java`, migrations `db/migration/v2.1.6_ndvi_job_queue.sql`, `v2.1.9_ndvi_job_force.sql`, `v2.2.1_ndvi_job_object_key.sql`.
- Historique : `model/isochrone/NdviObservation.java`, `repo/admin/NdviObservationRepository.java`, migration `db/migration/v2.1.7_ndvi_observation.sql`.
- Calcul OMS : `service/park/ComputeParkAreaV2.java` — `requestNdviRefresh()` dépose la demande après le `save` OMS.
- Traitement NDVI : `extern/ndvi/NdviBatchJobService.java` — lots de 10 toutes les 5 min, reprise `IN_PROCESS`/`IN_ERROR`, recopie sur toutes les années du parc et historisation de chaque réponse.
- Schéma : dev `ddl-auto: update` (`application-dev.yml`) ; prod `ddl-auto: none` + Flyway (`application.yml`) → migrations obligatoires.
- Lecture : `ParkAreaComputedRepository` standard (`findAll`/`findByIdAndAnnee`) + `NdviObservationRepository` pour l'historique, sans exposition front ; `DashboadService` insensible aux nouvelles colonnes.

## IMPACTS

- **IM01 — Schéma `park_area_computed` étendu** : colonnes NDVI (moyenne, min, max, écart-type, date d'acquisition ; nommage à aligner sur la convention existante au raffinement). Dev : auto (`ddl-auto: update`) ; prod : **migration Flyway obligatoire**.
- **IM02 — Nouvelle dépendance runtime côté backend** : le pipeline Java n'appelle plus directement le fournisseur ; un service planifié consomme la file `ndvi_job`. Le quota mensuel (10 000 requêtes) devient le facteur limitant pour les parcs de France si le volume dépasse le plafond.
- **IM03 — Pipeline de calcul découplé** : `ComputeParkAreaV2` ne subit plus la latence réseau ; il ne fait qu'un `save` + un enqueue. Le traitement NDVI est borné (10 jobs / 5 min) et reprenable après crash.
- **IM04 — Chemins de lecture inchangés** : repository standard + `DashboadService`/expositions restent insensibles aux nouvelles colonnes ; l'historique est lu via `NdviObservationRepository`, sans exposition front.
- **IM05 — Mutualisation du quota** : un seul appel Copernicus par parc, résultat recopié sur toutes les années de `park_area_computed`. Le fallback de fenêtre peut néanmoins consommer un second appel si la fenêtre principale ne renvoie aucune donnée.
- **IM06 — Nouvelle table d'historique** : `ndvi_observation` reçoit une ligne par appel retournant des statistiques (période, date d'acquisition, nombre de pixels, valeurs NDVI). Elle n'altère pas les lectures OMS existantes et sert de source pour une reventilation post-traitement et l'histoire végétale du parc.

> Traçabilité — `raises` : IM01 → IS04 · IM02 → IS01, IS03 · IM03 → IS02, IS03 · IM04 — sans capability (impact de lecture) · IM05 → IS05 · IM06 → IS06.

## ISSUES

- **IS01 — Point d'entrée statistique non précisé** : le fournisseur est retenu (Sentinel Hub, compte gratuit) mais aucun point d'entrée statistique par polygone ni son quota par appel n'est encore retenu. Sans cette valeur, S02 reste non implémentable.
  - *stems from* : IM02 · *requires* : D01
- **IS02 — Fraîcheur NDVI découplée des calculs OMS** : le recalcul OMS ne déclenche plus l'appel, il dépose une demande. Une ligne peu recalculée peut ne jamais être mise en file. Stratégie retenue pour le pipeline automatique : demande à partir de septembre, au plus une fois par an et par parc, portée par la ligne de référence ; le bouton « Calculer le parc » peut déposer une demande manuelle si aucun NDVI n'existe.
  - *stems from* : IM03 · *requires* : D02
- **IS03 — Latence / échec réseau hors pipeline OMS** : l'appel réseau est déplacé dans `NdviBatchJobService` ; un échec passe le job en `IN_ERROR` sans impacter la ligne OMS. Budget d'appel et reprise à fixer.
  - *stems from* : IM02, IM03 · *requires* : D03
- **IS04 — Migrations prod Flyway** : en prod `ddl-auto: none` — colonnes NDVI, table `ndvi_job` **et** table `ndvi_observation` doivent être créées par script append-only.
  - *stems from* : IM01, IM06 · *requires* : D04
- **IS05 — Quota mensuel insuffisant pour tous les parcs de France** : 10 000 requêtes/mois peut être inférieur au nombre de parcs. Mitigation : un seul appel par parc, recopie sur toutes les années, reprise quotidienne des jobs en erreur. Le dimensionnement reste à valider.
  - *stems from* : IM05 · *requires* : D05
- **IS06 — Reventilation des observations sur les bonnes années** : une observation NDVI est datée (période + acquisition) et ne doit pas être confondue avec l'année de la ligne `park_area_computed`. L'historique est nécessaire pour affecter correctement les valeurs lors d'un post-traitement.
  - *stems from* : IM06 · *requires* : D06

## DECISIONS

- **D01 — Mode d'obtention : API statistique du fournisseur** — statistiques calculées côté fournisseur sur le polygone du parc (pas de raster téléchargé, pas de calcul NDVI re-implémenté côté Java).
  - *addresses* : IS01 · *requires* : CA02
  - **D01.1 — Endpoint exact** + auth : `https://sh.dataspace.copernicus.eu/statistics/v1`, OAuth CDSE (`client-id`/`client-secret` configurables).
  - **D01.2 — Quota par appel / limites de taux** : compte gratuit CDSE, 10 000 requêtes/mois et 10 000 PU/mois ; seules les réponses 2XX sont décomptées.
  - **D01.3 — Convention de sortie** : moyenne, min, max, écart-type, date d'acquisition, période d'agrégation et nombre de pixels (`sampleCount`), mappés depuis la réponse statistique CDSE.
- **D02 — Politique de fraîcheur et d'exécution** : `ComputeParkAreaV2` ne fait aucun appel réseau ; il dépose une demande dans `ndvi_job` après le `save` OMS. Le traitement est asynchrone, par lots de 10 toutes les 5 min.
  - *addresses* : IS02 · *requires* : CA01, CA03, CA05
- **D03 — Stratégie d'échec en file** : timeout configurable ; erreur technique (réseau, HTTP, quota) → job `IN_ERROR`, ligne OMS intacte, reprise quotidienne ; job bloqué `IN_PROCESS` recyclé par une tâche toutes les 4h (seuil 8h15). Aucune exception ne remonte au calcul OMS.
  - *addresses* : IS03 · *requires* : CA02, CA03, CA04, CA05
  - **D03.1 — Valeur du timeout** : `application.ndvi.timeout-ms` (10 s par défaut).
- **D04 — Migrations prod par Flyway** : scripts append-only versionnés : colonnes NDVI (`v2.1.3`, `v2.1.5`), table `ndvi_job` (`v2.1.6`, colonne `force` en `v2.1.9`, clé par objet en `v2.2.1`) et table `ndvi_observation` (`v2.1.7`). `v2.2.1` ramène la file à un job par parc : la ligne la plus récente est conservée, la dernière date traitée toutes années confondues est reportée dessus. Flyway s'exécute au démarrage sur tous les profils (`flyway.enabled: true` global) ; `ddl-auto: update` complète le reste en dev.
  - *addresses* : IS04 · *requires* : CA01, CA05, CA07
- **D05 — Un seul appel par parc, toutes années alimentées** : le NDVI Copernicus étant lissé sur plusieurs années, la réponse est recopiée sur toutes les lignes `park_area_computed` du parc (même `ndvi_request_date`). Aucune demande ni aucun appel n'est fait si un NDVI est déjà présent sur le parc ; la valeur existante est recopiée sur les lignes qui n'en ont pas. Le fallback de fenêtre reste possible et peut doubler l'appel lors du premier remplissage.
  - *addresses* : IS05 · *requires* : CA06
- **D06 — Historiser chaque observation NDVI** : chaque réponse contenant des statistiques est enregistrée dans `ndvi_observation` avec le parc, la date d'appel, la période retournée, la date d'acquisition, le nombre de pixels et les valeurs NDVI. La copie sur toutes les années de `park_area_computed` reste inchangée ; l'historique sert de source pour une reventilation ultérieure et l'histoire végétale. Chaque appel retournant des statistiques crée une observation, sans dédoublonnage.
  - *addresses* : IS06 · *requires* : CA07
- **D07 — File NDVI par objet, sans NULL** : la clé de `ndvi_job` est `(id, id_inspire, object_type)`, avec une clé technique `job_id` (surrogate, `IDENTITY` côté entité). Aucune colonne de clé n'est NULL : `id = -1` si la ligne porte un `id_inspire`, `id_inspire = ''` si elle porte un `id`. Motif : Hibernate ne retrouve pas une ligne dont un composant de clé est NULL (`find` vide, donc demande dupliquée et insert en violation d'unicité). Seul `PARK_AREA_COMPUTED` est consommé ; les autres types restent en file (OOS8).
  - *addresses* : IS04 · *requires* : CA05

## CAPABILITIES

- **CA01 — Persistence des colonnes NDVI** : champs nullable sur l'entité `ParkAreaComputed` (moyenne, min, max, écart-type, date d'acquisition) + propagation JPA repository ; migration Flyway prod append-only.
  - *implements* : IM01, D04
- **CA02 — Client API statistique backend** : service Java appelant l'endpoint statistique du fournisseur pour un polygone de parc, configuré (endpoint/secret — D01.1), retournant des stats typées (moyenne, min, max, écart-type, date d'acquisition).
  - *implements* : IM02, D01, D03
- **CA03 — Intégration pipeline OMS découplée** : `ComputeParkAreaV2.computeGenericParkAreaV2()` dépose une demande idempotente dans `ndvi_job` après le `save` de la ligne OMS ; aucun appel réseau dans le pipeline.
  - *implements* : IM03, D02
- **CA04 — Résilience d'appel** : timeout configuré, erreur → job `IN_ERROR` + log WARN, exception jamais propagée au calcul OMS, reprise quotidienne.
  - *implements* : D03
- **CA05 — File persistante et reprise** : table `ndvi_job` (statuts `TO_PROCESS`/`IN_PROCESS`/`PROCESSED`/`IN_ERROR`), clé `(id, id_inspire, object_type)` sans NULL, clé technique `job_id`, traitement par lots de `PARK_AREA_COMPUTED`, recyclage des jobs bloqués et des erreurs.
  - *implements* : IM03, D02, D03, D04, D07
- **CA06 — Propagation toutes années** : un seul appel Copernicus par parc, réponse recopiée sur toutes les lignes `park_area_computed` du parc ; si un NDVI existe déjà, aucun appel n'est fait et la donnée est réutilisée.
  - *implements* : IM05, D05
- **CA07 — Historique des observations NDVI** : entité `NdviObservation`, repository `NdviObservationRepository`, migration `v2.1.7` ; `NdviBatchJobService` enregistre une ligne par réponse contenant des statistiques avant la recopie annuelle. Lecture par parc triée par période, sans exposition front.
  - *implements* : IM06, D06

### Reverse links
- IM01 *requires* CA01 · IM02 *requires* CA02, CA04 · IM03 *requires* CA03, CA05 · IM04 — sans capability (impact de lecture) · IM05 *requires* CA06 · IM06 *requires* CA07
- D01 *requires* CA02 · D02 *requires* CA01, CA03, CA05 · D03 *requires* CA02, CA03, CA04, CA05 · D04 *requires* CA01, CA05, CA07 · D05 *requires* CA06 · D06 *requires* CA07 · D07 *requires* CA05
- CA01 *is realized by* AV02 · CA02 *is realized by* AV01, AV03 · CA03 *is realized by* AV01, AV04 · CA04 *is realized by* AV04 · CA05 *is realized by* AV02, AV04 · CA06 *is realized by* AV02, AV03 · CA07 *is realized by* AV05

## ADDED VALUES

- **AV01 — Mesure objective du vivant dans le parc** : le NDVI quantifie la densité de végétation réelle (Rhew et al. 2011, r ≈ 0.69 vs cotation d'experts) — l'indicateur OMS (surface accessible) ne dit rien de la canopie : l'indice par parc ajoute cette dimension, mesurée par une source validée.
  - *realizes* : CA02, CA03
- **AV02 — Donnée au cœur du modèle** : un NDVI par parc, propagé sur toutes ses lignes `park_area_computed` = exploitable par toute analyse future sans nouveau pipeline ni re-calcul ad hoc, pour un seul appel fournisseur.
  - *realizes* : CA01, CA03, CA05, CA06
- **AV03 — zéro coût / zéro stockage raster** : statistiques calculées par le fournisseur gratuit ; aucun raster hébergé, aucun calcul NDVI maison.
  - *realizes* : CA02
- **AV04 — Pipeline toujours vivant** : un échec NDVI ne met jamais en péril les indicateurs OMS (file séparée, colonnes nulles, reprise quotidienne).
  - *realizes* : CA01, CA04
- **AV05 — Histoire végétale et reventilation** : chaque observation est conservée avec sa période et sa date d'acquisition, ce qui permet de rattacher un NDVI à la bonne année et de reconstituer l'évolution végétale du parc sans effacer les mesures antérieures.
  - *realizes* : CA07

## USER STORIES

> **Note de cadrage (réel relevé)** : le gardien `computeGenericHead` court-circuite le calcul OMS d'une ligne fraîche, mais `requestNdviRefresh` est appelé aussi dans ce cas : la demande NDVI est donc déposée sans recalculer l'OMS (voir 01.3 / CA03). L'appel fournisseur n'a lieu que dans `NdviBatchJobService`.

- **US01 — Indice NDVI disponible par parc/année** *(Medium — implements AV01, AV02, AV04)*
  As analyste consommateur de données, je veux les colonnes NDVI (moyenne, min, max, écart-type, date d'acquisition) peuplées sur chaque ligne de `park_area_computed`, afin d'exploiter un indicateur de végétation sans calcul ad hoc.

- **US02 — Client d'appel isolé et configurable** *(Low — implements AV03, AV04)*
  As mainteneur backend, je veux un service d'appel statistique configurable (endpoint, secret, timeout), afin de changer de réglage sans toucher au pipeline.

- **US03 — Migration Flyway prod append-only** *(Low — implements AV02, AV05)*
  As ingénieur de déploiement, je veux un script Flyway qui ajoute les colonnes NDVI, la table `ndvi_job` et la table `ndvi_observation` en prod, afin d'évoluer le schéma sans interruption ni destruction de données.

- **US04 — File d'attente NDVI avec reprise** *(Medium — implements AV02, AV04)*
  As exploitant, je veux que les demandes NDVI soient persistées et traitées par lots avec reprise, afin que le calcul OMS ne dépende jamais de la disponibilité du fournisseur et qu'aucune demande ne soit perdue.

- **US05 — Historique NDVI par parc** *(Medium — implements AV05)*
  As analyste, je veux conserver chaque observation NDVI avec sa période et sa date d'acquisition, afin de reconstituer l'histoire végétale du parc et de reventiler les valeurs lors d'un post-traitement.

### Scénarios Gherkin

#### US01 : remplir les colonnes NDVI
```gherkin
Scenario 01.1 : dépôt d'une demande au recalcul OMS
  Given la configuration NDVI est active
  And la ligne (parc, annee) correspond à l'année de référence
  When ComputeParkAreaV2 recalcule la ligne OMS
  Then la ligne OMS est sauvegardée avec ses indicateurs OMS
  And une demande est déposée dans ndvi_job avec le statut TO_PROCESS
  But aucun appel au fournisseur n'est fait dans le pipeline OMS

Scenario 01.2 : un appel fournisseur alimente toutes les années du parc
  Given un job NDVI TO_PROCESS pour le parc
  And le fournisseur renvoie des statistiques
  When NdviBatchJobService traite ce job
  Then un seul appel fournisseur est effectué pour ce parc
  And les colonnes NDVI sont recopiées sur toutes les lignes park_area_computed du parc, toutes années confondues
  And ndvi_request_date est posée sur toutes ces lignes
  And le job passe PROCESSED

Scenario 01.3 : demande NDVI sur une ligne OMS fraîche
  Given une ligne OMS fraîche (updated > park.updated, indicateurs OMS non-null)
  When ComputeParkAreaV2 parcourt ce parc
  Then la demande NDVI est déposée si elle est due
  And les valeurs OMS existantes restent inchangées

Scenario 01.4 : échec fournisseur non bloquant
  Given le fournisseur est injoignable ou le quota est épuisé
  When NdviBatchJobService traite le job
  Then le job passe IN_ERROR
  And un log WARN retrace l'échec pour ce parc
  And les lignes OMS et les colonnes NDVI restent inchangées
  But aucune exception n'est propagée au pipeline de calcul OMS

Scenario 01.5 : NDVI déjà présent, aucun nouvel appel
  Given un NDVI est déjà stocké sur au moins une ligne park_area_computed du parc
  When une demande NDVI est évaluée ou un job est traité
  Then aucun appel fournisseur n'est effectué
  And la donnée existante est recopiée sur les lignes du parc qui n'en ont pas
  And le job éventuel passe PROCESSED

Scenario 01.6 : réponse Copernicus historisée
  Given un job NDVI TO_PROCESS pour le parc
  And le fournisseur renvoie des statistiques avec une période et un nombre de pixels
  When NdviBatchJobService traite ce job
  Then une ligne ndvi_observation est créée pour ce parc
  And elle contient la date d'appel, la période, la date d'acquisition et les valeurs NDVI
  And les colonnes NDVI sont recopiées sur toutes les lignes park_area_computed du parc

Scenario 01.7 : pas d'historique sans donnée
  Given le fournisseur ne renvoie aucune statistique exploitable
  When NdviBatchJobService traite le job
  Then aucune ligne ndvi_observation n'est créée
  And ndvi_request_date est tout de même posée sur les lignes du parc
  And le job passe PROCESSED
```

#### US02 : appel isolé et configurable
```gherkin
Scenario 02.1 : valeurs lues depuis la configuration
  Given la configuration applicative définit endpoint, secret et timeout de l'API statistique
  When NdviBatchJobService traite un job
  Then l'appel statistique utilise ces valeurs
  And aucune valeur n'est écrite en dur dans le code du service d'appel

Scenario 02.2 : budget temporel respecté en cas d'échec
  Given un timeout T est configuré pour l'appel statistique
  When l'endpoint ne répond pas dans le délai T
  Then l'appel échoue au bout de T au maximum
  And le job passe IN_ERROR et sera rejoué
  And le calcul OMS n'est pas impacté
```

#### US03 : migration Flyway prod
```gherkin
Scenario 03.1 : activation des colonnes en production
  Given les scripts Flyway d'ajout des colonnes NDVI et des tables ndvi_job et ndvi_observation sont présents
  When le déploiement en production démarre
  Then les colonnes NDVI sont ajoutées avec des valeurs nulles par défaut
  And la table ndvi_job est créée avec sa clé technique job_id, son unicité (id, id_inspire, object_type) et son index (status, demand)
  And la table ndvi_observation est créée avec son index (park_area_id, period_start)
  And aucune donnée OMS existante n'est modifiée ou supprimée

Scenario 03.2 : cohérence des colonnes en développement
  Given la configuration de développement active ddl-auto update
  When l'application démarre en local
  Then les colonnes NDVI et les tables ndvi_job et ndvi_observation sont créées automatiquement avec le même nommage que les scripts Flyway
```

#### US04 : file d'attente et reprise
```gherkin
Scenario 04.1 : demande idempotente
  Given un job du parc déjà TO_PROCESS ou IN_PROCESS
  When une nouvelle demande NDVI est déposée pour le même parc
  Then aucun doublon n'est créé

Scenario 04.2 : reprise après crash
  Given un job resté IN_PROCESS au-delà du seuil de 8h15
  When la tâche de recyclage s'exécute
  Then le job repasse TO_PROCESS et sera traité au cycle suivant

Scenario 04.3 : reprise après erreur fournisseur
  Given un job IN_ERROR depuis plus de 24h
  When la tâche de recyclage quotidienne s'exécute
  Then le job repasse TO_PROCESS
  And il est rejoué au prochain lot

Scenario 04.4 : demande manuelle depuis le bouton « Calculer le parc »
  Given un parc sans NDVI
  When l'utilisateur déclenche le bouton « Calculer le parc »
  Then un job NDVI est déposé pour le parc
  And aucun job NDVI n'est déposé si un NDVI existe déjà sur le parc
```

#### US05 : histoire végétale
```gherkin
Scenario 05.1 : lecture de l'historique d'un parc
  Given plusieurs observations NDVI ont été enregistrées pour un parc
  When on interroge l'historique par park_area_id
  Then les observations sont retournées de la plus ancienne à la plus récente
  And chaque observation porte sa période, sa date d'acquisition et ses statistiques

Scenario 05.2 : reventilation post-traitement
  Given une observation NDVI avec une date d'acquisition connue
  When un post-traitement affecte les observations aux années du parc
  Then l'observation peut être rattachée à la bonne période
  And la copie annuelle existante n'est pas utilisée comme seule source
```

### Liens AV · US
- AV01 *is implemented by* US01 · AV02 *is implemented by* US01, US03, US04 · AV03 *is implemented by* US02 · AV04 *is implemented by* US01, US02, US04 · AV05 *is implemented by* US03, US05

## DEPENDENCIES

### Hard
- **DH1** — Migrations Flyway prod écrites + appliquées (colonnes NDVI, table `ndvi_job` **et** table `ndvi_observation` ; sinon entity/table absentes ou file/historique inutilisables — D04).
- **DH2** — Instance/endpoint statistique du fournisseur disponible + secret configuré (valeurs D01.1/D01.3 — héritées de l'epic 1) ; sinon rafraîchissement NDVI impossible (colonnes restent nulles, S04 tient).
- **DH3** — Deploy backend `geoservice-isochrone-v2` (première fois où le backend gagne une dépendance externe — Epic 1 était front-only).
- **DH4** — Payload réponse du fournisseur conforme D01.3 (statistiques, période d'agrégation et nombre de pixels : moyenne, min, max, écart-type, date d'acquisition, `sampleCount`).
- **DH5** — Planification active (`@EnableScheduling` déjà présent dans `SchedulerConfig`) ; sans elle, la file `ndvi_job` ne serait jamais consommée.

### Soft
- **DS1** — Doc fournisseur : quota par appel (D01.2) et limites de taux — afin de dimensionner le volume pipeline/parcs.
- **DS2** — Coiner du timeout D03.1 après mesure de latence réelle sur les parcs.
- **DS3** — Épic 1 : réutilisation du compte/fournisseur sans rétro-impact technique (aucun lien de traçabilité inter-doc, périmètres séparés).
- **DS4** — Dimensionner le lot (`application.ndvi.batch.delay-seconds`, défaut 300 s) et le seuil de reprise selon le nombre réel de parcs et le quota mensuel.
- **DS5** — Surveiller la volumétrie de `ndvi_observation` (une ligne par appel retournant des statistiques, sans dédoublonnage) et prévoir une purge/archivage si nécessaire.

## APPENDIX

### Sources
1. Rhew et al. 2011 — validité du NDVI comme mesure objective de végétation (r ≈ 0.69) — https://pmc.ncbi.nlm.nih.gov/articles/PMC3225119/
2. D'Silva 2025 — NDVI vs EVI en zone dense ; stats passées au filtre zonal par ville — https://papers.ssrn.com/sol3/papers.cfm?abstract_id=5165379
3. NASA Earth Observatory — Interprétation des valeurs NDVI et sensibilité aux nuages — https://science.nasa.gov/earth/earth-observatory/measuring-vegetation-ndvi-evi/

### Ancrage code (vérifié à `36fd155` / branche `feat/ndvi-front`)
- `model/isochrone/ParkAreaComputed.java` — entité clé `(id, annee)`, colonnes NDVI nullable.
- `service/park/ComputeParkAreaV2.java` — guard `updated` (calcul OMS), `requestNdviRefresh()` après `save`, filtres année de référence et demande due (à partir de septembre, 1×/an).
- `extern/ndvi/NdviBatchJobService.java` — file `ndvi_job`, lots de 10 toutes les 5 min, reprise `IN_PROCESS` (seuil 8h15) et `IN_ERROR` (quotidienne), recopie sur toutes les années du parc, historisation dans `ndvi_observation`.
- `extern/ndvi/NdviStatistics.java` — statistiques + période d'agrégation + `sampleCount`.
- `extern/ndvi/ClientCopernicusNdvi.java` — client CDSE, quota mensuel, fenêtre principale + fallback.
- `model/admin/NdviJob.java` · `repo/admin/NdviJobRepository.java` · `db/migration/v2.1.6_ndvi_job_queue.sql`, `v2.1.9_ndvi_job_force.sql`, `v2.2.1_ndvi_job_object_key.sql` — file persistante (clé par objet, sentinelles `-1` / `''`).
- `model/isochrone/NdviObservation.java` · `repo/admin/NdviObservationRepository.java` · `db/migration/v2.1.7_ndvi_observation.sql` — historique des observations.
- `repo/ParkAreaComputedRepository.java` — repository standard + `findAllByIdPark(id)` pour la propagation multi-années.
- `application-dev.yml` (ddl-auto update) · `application.yml` (`application.ndvi.*`, flyway, prod ddl none).

## DOCUMENT METADATA

| Champ | Valeur |
|---|---|
| Epic | NDVI par parc — colonne ParkAreaComputed |
| Slug | `ndvi-par-parc-park-area-computed` |
| Product Owner | cunvoas |
| Trimestre cible | T4 2026 |
| Statut | Framing v1.3 — file `ndvi_job` par objet sans NULL, un job par parc (2026-11-08) |
| Date | 2026-11-08 |
| Base code | repo `cunvoas/opendata-tools`, branche `feat/ndvi-front` @ `36fd155` · backend `geoservice-isochrone-v2` concerné |
| Décisions ouvertes | Aucune (D01.1–D07 renseignées) |
| Epic lié (valeur générée) | `recuperation-ndvi-parc-framing.md` (fournisseur Sentinel Hub, front overlay) |
| Version | 1.3 |
