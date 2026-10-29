# Framing — Publication GeoJSON des EPCI vers le dépôt GitHub Pages

**Epic :** Publication des fichiers GeoJSON pour l'application dans un répertoire + publication GitHub depuis la page d'admin des EPCI (slug : `publication-geojson-github-epci`)
**Product Owner :** cunvoas · **Trimestre cible :** T4 2026 · **Statut :** cadrage en cours

## SCOPE

### In scope

- S01 — Branchement de la publication GitHub au bout du flux existant : après l'écriture des fichiers GeoJSON par `ServicePublicationExporter` dans `{application.admin.export-data-path}/geojson/<type>/<com2coId>/`, appel de `GitHelper.addCommitPush()` (composant JGit déjà écrit et testé, jamais câblé en production) depuis `PublishService.publishAsync`.
- S02 — Périmètre de commit borné : seuls les sous-dossiers du répertoire d'export correspondant à l'EPCI publié (parkOutline, isochrone, carreaux, IRIS, stats par ville et stats globales pour `com2coId` et l'année demandée) sont stage/commit/push — jamais le dépôt entier.
- S03 — Correction des défauts bloquants de `GitHelper` dans le même epic :
  - `@Value("application.git.GIT_USERNAME")` sans `${}` → injecte la chaîne littérale, la configuration n'est jamais lue ;
  - incohérence des clés : la configuration expose `application.git.username/token/localPath/defaultBranch` (`application-pi_nuc.yml:69`, `application-prod.yml:45`) alors que `GitHelper` lit `GIT_USERNAME`/`GIT_TOKEN`.
- S04 — Feedback admin : le résultat du push (succès/échec, commit SHA) remonte dans le statut consultable depuis la page d'édition EPCI (`editCommunauteCommune.html` → `checkPublishStatus()`), au lieu d'un échec silencieux dans la tâche asynchrone.
- S05 — Traçabilité : une ligne de log structurée par publication (EPCI, année, nombre de fichiers, résultat du push, durée) pour l'audit des données publiées.

### Out of scope (avec raison)

| # | Hors périmètre | Raison |
|---|---|---|
| OOS1 | Modification de la page admin vue3 (`geoservice-map-vue3`) | Aucune page admin EPCI n'existe côté front public ; l'admin est le MVC Thymeleaf du backend, le bouton « Publier » et le polling de statut existent déjà |
| OOS2 | Régénération ou changement du contenu des GeoJSON | `ServicePublicationExporter` produit déjà les fichiers attendus par le site GitHub Pages (`autmel.github.io/parcs-et-jardins`) ; l'epic ne change que le transport |
| OOS3 | Publication automatique déclenchée par les imports batch (carreaux, IRIS, overpass) | La publication reste volontaire depuis l'admin, derrière la garde `canPublish` ; un déclenchement auto saperait cette garde |
| OOS4 | GitHub Actions / workflow CI de publication | Un push JGit direct depuis l'instance suffit ; un workflow ajouterait un point de configuration de plus pour aucun gain |
| OOS5 | Migration vers l'API REST GitHub (au lieu de JGit) | `GitHelper` + JGit existent, sont testés, et le working copy est déjà un clone local ; une migration API n'apporterait rien |
| OOS6 | Publication multi-EPCI en lot (tous les EPCI d'une région) | Non demandé ; le flux actuel est un EPCI à la fois, garde-fou `canPublish` inclus |

### Références techniques mobilisées

1. `geoservice-isochrone-v2/docs/publication.md` — description du flux asynchrone existant et des chemins d'export.
2. `geoservice-isochrone-v2/src/test/java/.../extern/helper/TestGitHelper.java` — contrat attendu de `addCommitPush` (stage récursif, message de commit, credentials, cas « credentials missing »).
3. Site GitHub Pages de référence : `https://autmel.github.io/parcs-et-jardins`, dépôt working-copy sibling `gh_pages/geoservice-data` (dev : `application.yml:180,183`) ; prod : `/var/isochrone/data` (montage `MOUNT_PATH`).

## IMPACTS

- **IM01 — Backend `geoservice-isochrone-v2` modifié** : `PublishService.publishAsync` gagne une étape de publication Git après l'écriture des fichiers ; `GitHelper` est corrigé et consommé en production pour la première fois.
- **IM02 — Configuration requise en production** : `application.git.username`, `application.git.token` (PAT avec droits push), `application.git.localPath` doivent être fournis par variables d'environnement (`GIT_USERNAME`, `GIT_TOKEN`, `MOUNT_PATH`) sur chaque environnement où la publication est activée.
- **IM03 — Le dépôt GitHub Pages devient la cible directe d'une écriture applicative** : chaque clic « Publier » produit un commit sur la branche `defaultBranch` → historique du dépôt public pollué si clics répétés, besoin d'idempotence (un seul commit par publication, pas un par fichier).
- **IM04 — Contenu public modifié automatiquement** : les fichiers poussés sont immédiatement servis aux visiteurs via GitHub Pages → une erreur métier validée comme publiée devient visible sans relecture humaine ; la garde `canPublish` (batchs terminés sans erreur) est le seul filtre.
- **IM05 — Déploiement backend** : release Java + variables d'environnement Git ; aucun changement de schéma, aucun changement de contrat d'API (les endpoints `/mvc/ajax/publish/api/*` gardent leur signature).

> Traçabilité — `raises` : IM01 → IS01, IS02 · IM02 → IS03 · IM03 → IS04 · IM04 → IS05 · IM05 → (impact de déploiement, sans issue dédiée). `requires` : IM01 → CA01, CA02 · IM02 → CA03 · IM03 → CA04 · IM04 → CA04 · IM05 → CA03.

## ISSUES

- **IS01 — `GitHelper` n'est appelé nulle part en production** : le chaînon « fichiers écrits → dépôt GitHub » n'existe pas ; aujourd'hui la mise en ligne est une copie manuelle (`cp -Rf /var/isochrone/data/* … && git status`, cf. `TestExporterApplication.java:30-31`). Sans branchement, l'epic ne change rien.
  - *stems from* : IM01 · *requires* : D01
- **IS02 — Défauts de `GitHelper`** : `@Value` sans `${}` (littéral injecté) et clés de configuration incohérentes (`application.git.*` vs `GIT_USERNAME`/`GIT_TOKEN`). Si branché en l'état : push systématiquement en échec d'authentification.
  - *stems from* : IM01 · *requires* : D02
- **IS03 — Environnements sans clone Git monté** : en dev le `export-data-path` est le working-copy sibling ; en prod c'est `/var/isochrone/data` monté (`MOUNT_PATH`) — si le montage n'est pas le clone du dépôt ou si le token manque, le push échoue. Ratification PO : l'écriture des fichiers ne doit jamais dépendre du succès du push ; l'échec du push est remonté au statut, jamais avalé.
  - *stems from* : IM02, IM05 · *requires* : D03
- **IS04 — Pollution de l'historique du dépôt public** : publications répétées ou en retry créent une rafale de commits « automatic data update » sur le dépôt public. Ratification PO : un seul commit par publication (fichiers de tous les types de l'EPCI dans un commit), message horodaté existant conservé.
  - *stems from* : IM03 · *requires* : D04
- **IS05 — Publication sans relecture humaine** : le push est immédiat et public ; la seule validation est `canPublish` (tous les batchs traités sans erreur, `processed>0`). Ratification PO : garde conservée telle quelle, aucune étape de relecture ajoutée (outil interne, responsabilité de l'administrateur).
  - *stems from* : IM04 · *requires* : D05

## DECISIONS

- **D01 — Brancher la publication Git dans `PublishService.publishAsync`, après l'écriture des fichiers et les stats.** La publication devient atomique du point de vue de l'admin : « Publier » = fichiers écrits + push GitHub, statut unique.
  - *addresses* : IS01 · *requires* : CA01
  - **D01.1 — Ordre exact des étapes async** : (1) `writeGeoJsonCarreaux` + 3 stats, (2) `addCommitPush` sur les dossiers de l'EPCI, (3) enregistrement du résultat → statut. Échec à l'étape 2 : fichiers conservés sur disque, statut « publié localement, push en échec » avec message.
  - **D01.2 — Étendue du stage** : référentiels (`data/regions.json`, `data/com2cos`, `data/cities`), puis tous les dossiers produits pour l'EPCI et l'année — `geojson/parkOutline/<id>`, `geojson/isochrones/<id>`, `geojson/carres/<id>`, `geojson/iris/<id>`, `data/stats/com2co/<id>` et `data/stats/<dept>/<insee>` des villes de l'EPCI (séquence de `TestExporterApplication#export`).
  - **D01.3 — Statut de production distinct avant la publication Git** : le statut de publication expose une phase de production (écriture des GeoJSON + stats, fichier par fichier : type, succès/échec, date, taille) et la publication Git n'est tentée que si la production est complète sans erreur. L'admin voit donc toujours « production » et « publication » comme deux étapes séparées, et peut consulter la production des fichiers même si le push n'a pas encore eu lieu (ou a échoué / n'est pas configuré).
- **D02 — Corriger `GitHelper` avant tout branchement** : `@Value("${application.git.username}")` / `@Value("${application.git.token}")` (clés de configuration existantes), suppression des constantes `GIT_USERNAME`/`GIT_TOKEN` au profit de la config ; test existant `TestGitHelper` mis à jour.
  - *addresses* : IS02 · *requires* : CA02
- **D03 — Résilience ratifiée par le PO** : l'échec du push ne détruit ni ne corrompt les fichiers écrits ; le statut admin expose l'erreur (message JGit tronqué) et un nouveau clic « Publier » retente ; pas de retry automatique.
  - *addresses* : IS03 · *requires* : CA01, CA04
- **D04 — Un commit par publication** : tous les fichiers de l'EPCI/année dans un seul commit `chore(<folder>): automatic data update <ISO datetime>` (message existant de `GitHelper`, inchangé) ; push sur `application.git.defaultBranch`.
  - *addresses* : IS04 · *requires* : CA01
- **D05 — Garde `canPublish` inchangée** : le bouton reste désactivé tant que les batchs ne sont pas tous traités sans erreur ; le token d'administration (`TokenManagement.isTokenValid()`) reste obligatoire sur l'endpoint de demande.
  - *addresses* : IS05 · *requires* : CA04

> Crédit de source : flux existant documenté dans `geoservice-isochrone-v2/docs/publication.md` et workflow manuel documenté dans `TestExporterApplication.java` — l'epic remplace la copie manuelle, il n'invente pas de nouveau contrat de données.

## CAPABILITIES

- **CA01 — Publication Git en bout de flux async** : `PublishService.publishAsync` appelle `GitHelper.addCommitPush` sur les dossiers de l'EPCI publié après l'écriture des GeoJSON ; un seul commit, push sur la branche par défaut, résultat capturé (SHA ou erreur) pour le statut ; publication Git conditionnée au statut de production « complet sans erreur » (D01.3).
  - *implements* : D01, D03, D04
- **CA02 — `GitHelper` corrigé et configuré** : lecture des credentials et du chemin du dépôt depuis `application.git.*` (via `@Value("${...}")` alignés sur les yml existants), cas « credentials manquants » traité (erreur explicite, pas de push tenté) ; tests à jour.
  - *implements* : D02
- **CA03 — Configuration par environnement** : `GIT_USERNAME`, `GIT_TOKEN`, `MOUNT_PATH` documentés ; environnement sans configuration Git complète → publication de fichiers OK, push marqué « non configuré » dans le statut (dégradation propre, pas de crash async).
  - *implements* : D02, D03
- **CA04 — Statut & garde admin** : le statut de publication (endpoint `/status` existant) expose deux phases séparées : production (état de chaque fichier produit : type, succès/échec, date, taille) puis publication Git (push OK / push en échec / push non configuré, SHA de commit) ; garde `canPublish` et token inchangés.
  - *implements* : D01, D03, D05

### Reverse links
- IM01 *requires* CA01, CA02 · IM02 *requires* CA03 · IM03 *requires* CA01, CA04 · IM04 *requires* CA04
- D01 *requires* CA01, CA04 · D02 *requires* CA02, CA03 · D03 *requires* CA01, CA03, CA04 · D04 *requires* CA01 · D05 *requires* CA04
- CA01 *is realized by* AV01, AV02 · CA02 *is realized by* AV01 · CA03 *is realized by* AV03 · CA04 *is realized by* AV02, AV04

## ADDED VALUES

- **AV01 — Fin de la copie manuelle** : la mise en ligne des données devient un clic admin au lieu d'une procédure SSH (`cp -Rf … && git status`) réservée au mainteneur.
  - *realizes* : CA01, CA02
- **AV02 — Données publiées traçables** : chaque publication correspond à un commit identifiable (horodaté, SHA remonté dans l'admin) → audit et retour arrière possibles via `git revert`.
  - *realizes* : CA01, CA04
- **AV03 — Opérable sur tous les environnements** : environnement sans config Git dégrade proprement (fichiers écrits, push signalé non configuré) au lieu d'échouer silencieusement.
  - *realizes* : CA03
- **AV04 — Confiance préservée** : la garde de complétude des batchs et le token admin restent les seules portes d'entrée de la publication publique.
  - *realizes* : CA04

## USER STORIES

- **US01 — Publier un EPCI vers GitHub** *(Medium — implements AV01, AV02)*
  As administrateur, je veux que le bouton « Publier » écrive les GeoJSON de l'EPCI et les pousse en un commit vers le dépôt GitHub Pages, afin de mettre les données en ligne sans procédure manuelle.
- **US02 — Voir le résultat de la publication** *(Medium — implements AV02)*
  As administrateur, je veux voir dans le statut si les fichiers sont écrits, si le push a réussi (et son SHA) ou échoué, afin de savoir si les données sont réellement en ligne.
- **US03 — Retenter une publication échouée** *(Low — implements AV03)*
  As administrateur, je veux pouvoir recliquer « Publier » après un échec de push, afin de terminer la publication sans intervention technique.
- **US04 — Environnement sans dépôt Git configuré** *(Low — implements AV03)*
  As mainteneur, je veux que l'application démarre et publie les fichiers localement même sans credentials Git, afin que la configuration Git ne soit bloquante que pour le push.
- **US05 — Garde de complétude inchangée** *(Low — implements AV04)*
  As administrateur, je veux que la publication reste impossible tant que les batchs de l'EPCI ne sont pas tous traités sans erreur, afin de ne jamais publier des données incomplètes.

### Scénarios Gherkin

#### US01 : publier un EPCI vers GitHub
```gherkin
Scenario 01.1 : publication complète réussie
  Given l'EPCI « <nom> » a tous ses batchs traités sans erreur (canPublish actif)
  And la configuration Git est complète (username, token, localPath pointant sur le clone du dépôt)
  When l'administrateur clique « Publier » avec un token valide
  Then les fichiers GeoJSON de l'EPCI et de l'année sont écrits dans le répertoire d'export
  And un unique commit contenant tous ces fichiers est créé avec le message « chore(<folder>): automatic data update <ISO datetime> »
  And le commit est poussé sur la branche par défaut
  And le statut de publication indique « push OK » avec le SHA du commit

Scenario 01.2 : périmètre de commit limité à l'EPCI publié
  Given des fichiers d'autres EPCI sont modifiés dans le working-copy du dépôt
  When la publication de l'EPCI « <nom> » est exécutée
  Then seuls les fichiers de l'EPCI publié (parkOutline, isochrone, carreaux, IRIS, stats par ville et stats globales de l'année demandée) sont stage et commités
  And les modifications d'autres EPCI ne sont pas incluses dans le commit
```

#### US02 : voir le résultat de la publication
```gherkin
Scenario 02.1 : échec de push remonté à l'admin
  Given la publication de l'EPCI est en cours et les fichiers sont écrits
  When le push JGit échoue (credentials refusés, réseau, dépôt non à jour)
  Then les fichiers écrits restent intacts sur le disque
  And le statut de publication indique « push en échec » avec le message d'erreur
  And la tâche asynchrone ne lève pas d'exception silencieuse

Scenario 02.2 : statut de production consultable avant la publication Git
  Given la publication de l'EPCI est en cours
  When la page d'édition EPCI interroge le statut via /mvc/ajax/publish/api/status
  Then le statut expose la phase de production : pour chaque fichier produit, le type, le succès ou l'échec, la date et la taille
  And tant que la production n'est pas complète, aucun push Git n'est tenté

Scenario 02.3 : résultat de publication Git visible après production
  Given la phase de production est terminée sans erreur
  When la page d'édition EPCI interroge le statut via /mvc/ajax/publish/api/status
  Then le statut expose le résultat Git (push OK / push en échec / push non configuré) et le SHA si disponible
```

#### US03 : retenter une publication échouée
```gherkin
Scenario 03.1 : nouvelle tentative après échec
  Given une publication précédente s'est terminée en « push en échec »
  When l'administrateur reclique « Publier » avec un token valide
  Then les fichiers sont réécrits et un nouveau push est tenté
  And en cas de succès le statut passe à « push OK »
```

#### US04 : environnement sans dépôt Git configuré
```gherkin
Scenario 04.1 : dégradation propre sans credentials
  Given la configuration Git est absente ou incomplète (username ou token ou localPath manquant)
  When l'administrateur demande une publication
  Then les fichiers GeoJSON sont écrits normalement
  And le statut indique « push non configuré » sans tentative de push
  And aucune exception n'interrompt la tâche asynchrone
```

#### US05 : garde de complétude inchangée
```gherkin
Scenario 05.1 : publication refusée tant que les batchs sont incomplets
  Given l'EPCI a des batchs en cours, en erreur ou non traités
  When l'administrateur consulte la page d'édition EPCI
  Then le bouton « Publier » est désactivé
  And une demande de publication avec un token valide est refusée par /request
```

### Liens AV · US
- AV01 *is implemented by* US01 · AV02 *is implemented by* US01, US02 · AV03 *is implemented by* US03, US04 · AV04 *is implemented by* US05

## DEPENDENCIES

### Hard
- **DH1** — Release backend `geoservice-isochrone-v2` : le branchement async + la correction `GitHelper` sont déployés ensemble.
- **DH2** — Working-copy Git du dépôt GitHub Pages montée et accessible en écriture sur chaque environnement de publication (`MOUNT_PATH` en prod ; sibling `gh_pages/geoservice-data` en dev).
- **DH3** — PAT GitHub avec droits push sur le dépôt Pages, fourni via `GIT_TOKEN` (et `GIT_USERNAME`) sur les environnements activateurs.

### Soft
- **DS1** — Alignement éventuel des clés de configuration entre yml (`application.git.*`) et code : couvert par D02, non bloquant si fait dans l'epic.
- **DS2** — Mise à jour de `geoservice-isochrone-v2/docs/publication.md` pour documenter le nouveau flux (push automatique, statut, dégradation sans config) : recommandée, non bloquante.
- **DS3** — D01.2 (liste exacte des chemins de stats stage) : `TODO raffinement`, non bloquant pour le squelette du flux.

## APPENDIX

### Sources
1. Code existant : `PublishRestControler.java` (`/mvc/ajax/publish/api`), `PublishService.java` (`publishAsync`), `ServicePublicationExporter.java`, `GitHelper.java` (`addCommitPush`), `CommunauteCommuneControler.java` (`canPublish`), `editCommunauteCommune.html` (bouton « Publier » + polling statut).
2. Configuration : `application.yml:180,183` (dev, `export-data-path` = working-copy gh_pages), `application-pi_nuc.yml:69`, `application-prod.yml:45` (clés `application.git.*`).
3. Test existant : `TestGitHelper.java` (mocks JGit, cas credentials missing).
4. Site public servi : `https://autmel.github.io/parcs-et-jardins`.

### Snippet PO (backend, condensé)
- Flux visé : `publishAsync` → `servicePublicationExporter.writeGeoJsonCarreaux(com2co, annee)` + stats → `gitHelper.addCommitPush(repoRoot, "geojson/<type>/<com2coId>")` (par type, un commit — ou stage multi-dossiers si l'API JGit le permet) → statut.
- Corrections attendues dans `GitHelper` : `@Value("${application.git.username}")`, `@Value("${application.git.token}")` ; suppression des littéraux `GIT_USERNAME`/`GIT_TOKEN` ; credentials via `UsernamePasswordCredentialsProvider(username, token)`.
- Placeholders non résolus à la date de cadrage : liste exacte des chemins de stats (D01.2), environnement activateur par défaut.

## DOCUMENT METADATA

| Champ | Valeur |
|---|---|
| Epic | Publication des fichiers GeoJSON + publication GitHub depuis la page d'admin des EPCI |
| Slug | `publication-geojson-github-epci` |
| Product Owner | cunvoas |
| Trimestre cible | T4 2026 |
| Statut | Framing v1.0 — toutes couches rédigées, Quality Gate passé (2026-10-27) |
| Date | 2026-10-27 |
| Base code | repo `cunvoas/opendata-tools`, branche `feat/ndvi` @ `21f361c` (backend `geoservice-isochrone-v2` uniquement) |
| Décisions ouvertes | Aucune (D01.2 tranchée : séquence complète de `TestExporterApplication#export`) |
| Version | 1.0 |
