# Geoservice Isochrone v2 Summary

## Document Status

- **Status:** Draft for review
- **Profile:** API service (Spring MVC + REST) avec worker planifié embarqué et publication asynchrone. Lecture orientée « données d'urbanisme » : rédigé pour être lisible par un journaliste spécialisé urbanisme sans sacrifier la traçabilité technique.
- **Intended owner:** cunvoas (mainteneur unique, projet associatif AGPL-3.0)
- **Scope:** répertoire `geoservice-isochrone-v2/` du dépôt [cunvoas/opendata-tools](https://github.com/cunvoas/opendata-tools) (artefact Maven `com.github.cunvoas:geoservice-isochrone:2.0.11`)
- **Completeness:** Complete ; ingestion par ville (MEL, Lyon, Nantes, Toulouse) traitée au niveau catalogue ; framings front NDVI couverts au niveau « cadrage »
- **Last reviewed:** 2026-10-29
- **Evidence baseline:** branche par défaut `main` (= `origin/main`), arbre de travail sur `feat/ndvi-front` pointant le même commit (seuls fichiers d'IDE non commités : `.project`, `.settings/`, `.gitignore`). Lignes métier inchangées dans l'arbre de travail.
- **Validated commit:** `3a4855944dd7e9311f213c1773cc42d76940b775`

## Purpose and Boundaries

Ce service mesure et publie l'**accessibilité aux espaces verts des habitants**, confrontée aux seuils de l'Organisation mondiale de la santé (OMS) : en zone urbaine dense, un grand espace vert doit être joignable à pied en 5 minutes ; en zone périurbaine, en 20 minutes. Pour chaque micro-territoire (maille de 200 m × 200 m de l'INSEE, et district statistique IRIS), l'outil calcule la surface de parc accessible par habitant, l'écart aux normes OMS et les surfaces manquantes.

Il est conçu pour un usage **national** (toutes les EPCI de France), avec une saisie **participative** : habitants et associations complètent les parcs de leur territoire quand les données de leur collectivité sont absentes ou incomplètes. L'étude a démarré sur la Métropole Européenne de Lille (MEL), où les données sources contenaient des erreurs : des études contradictoires (Préfecture du Nord, Direction du Territoire) ont permis de **requalifier** certaines données sans les effacer — les corrections sont traçables, pas un simple « écrasement ».

Le service :

- ingère les données ouvertes françaises (INSEE, IGN, OpenStreetMap, cadastre, portails de collectivités) ;
- calcule les isochrones piétons (zone atteignable à pied depuis chaque entrée de parc, via l'API isochrone de l'IGN) ;
- calcule les indicateurs OMS (m²/hab, déficit, « parcs durables » de ≥ 5 000 m² à 300 m) ;
- enrichit chaque parc d'un indicateur de végétation **NDVI** mesuré par satellite (Sentinel-2 / Copernicus) ;
- propose des aménagements (10 algorithmes de résolution + propositions dessinées à la main) avec évaluation d'impact OMS ;
- publie les résultats en fichiers statiques (GeoJSON + statistiques) consommés par le site public [autmel.github.io/parcs-et-jardins](https://autmel.github.io/parcs-et-jardins).

Possède : sa base PostgreSQL/PostGIS (migrations Flyway), sa file de calcul, son domaine « parc/accessibilité », son code de calcul, sa console d'administration.

Ne possède pas : les portails de données sources, le site public lui-même (dépôt GitHub Pages), l'instance PostgreSQL, le serveur Matomo (analytics) ou le service Mailjet (emails).

## Component Context

| Relation | Composant ou système | Rôle |
| --- | --- | --- |
| Données amont | INSEE (grille 200 m, IRIS, densité, population carroyée Filosofil 2015/2017/2019/2021), IGN (topo végétal, API isochrone v2), OpenStreetMap (Overpass), cadastre.data.gouv.fr, portails open data des collectivités | Formes de référence, population, calculs d'isochrones |
| Données satellite | Copernicus Data Space Ecosystem (API statistique Sentinel-2 L2A) | Indice NDVI par parc (végétation réelle) |
| Stockage | PostgreSQL + PostGIS | Données géographiques sources et calculées, file de jobs, migrations v1.0.0 → v2.1.5 |
| Sortie | Export statique (GeoJSON + statistiques OMS) publié vers le dépôt GitHub Pages | Données consultables par le grand public |
| Services sortants | API isochrone IGN, adresse.data.gouv.fr (géocodage), Mailjet (emails contributeurs), Matomo (optionnel) | Calcul, recherche d'adresse, courriel du contributeur, mesure d'audience |
| Utilisateurs | Contributeurs (création/édition de parcs), administrateurs (console), anonymes (carte de consultation) | Pages Thymeleaf + Leaflet servies par ce service |

Runtime : un jar Spring Boot 4.1.1 / Java 21 (Dockerisé, uid non-root 1001), Tomcat port 8980, planificateur mono-thread, pool Hikari max 50 connexions PostGIS.

## Architecture

| Zone | Responsabilité | Source |
| --- | --- | --- |
| Point d'entrée | Application de démarrage et configuration | [GeoserviceIsochroneApplication.java](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/src/main/java/com/github/cunvoas/geoserviceisochrone/GeoserviceIsochroneApplication.java) |
| Pages MVC | Gestion des parcs (liste/nouvelle/édition/vérification), carte de consultation, entrées, admin (villes, EPCI, contributeurs, calculs), simulateur de projets, propositions manuelles | `src/main/java/com/github/cunvoas/geoserviceisochrone/controller/mvc/` |
| REST / GeoJSON | Flux GeoJSON pour la carte (parcs, IRIS, carreaux INSEE, cadastre, topo IGN), calculs AJAX, publication, exporteur, adresses | `controller/geojson/`, `controller/rest/` |
| Orchestration des jobs | File de calcul des carreaux 200 m et IRIS, recyclage des erreurs, arrêt propre | [BatchJobService.md](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/docs/BatchJobService.md) |
| Calcul géospatial | Stratégies v3/v4 de calcul par carreau, calcul IRIS, calcul par parc | [ComputeCarreServiceV4.md](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/docs/ComputeCarreServiceV4.md) |
| Solveurs de propositions | 10 algorithmes pour « quel parc créer ou agrandir pour tenir l'OMS » | `service/solver/`, [proposal-computation-strategy.md](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/docs/proposal-computation-strategy.md) |
| NDVI | Client API statistique Copernicus CDSE, quota mensuel persisté, fenêtre estivale glissante | `extern/ndvi/`, [copernicus-ndvi-oauth.md](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/docs/copernicus-ndvi-oauth.md) |
| Intégration Overpass | Parseurs OpenStreetMap (brut + GeoJSON) — raccord au calcul encore ouvert | `extern/overpass/` |
| Publication | Export asynchrone GeoJSON + statistiques puis commit/push Git du dépôt Pages | [publication.md](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/docs/publication.md) |
| Persistance | Spring Data JPA + Hibernate Spatial | `repo/` |
| Sécurité | Form login, hachage Argon2, cookie CSRF, limitation des tentatives de connexion, rôles | `config/security/` |
| Migrations | Flyway SQL v1.0.0 → v2.1.5, exécutées au démarrage | `src/main/resources/db/migration/` |

## Interfaces

Entrantes :

- Pages web (Thymeleaf) : gestion des parcs, carte de consultation, console admin, simulateur de projets, éditeur de propositions manuelles.
- REST AJAX protégés (authentification + token + rôle admin) : déclencher des calculs (`/mvc/ajax/jobs/api/request`), publier (`/mvc/ajax/publish/api/request`, `/publish/api/status`).
- Flux GeoJSON de la carte : parcs, propositions, IRIS, carreaux INSEE, cadastre, topo végétal.
- Exporteur de fichiers : protégé par clé + liste d'IP autorisées (documenté comme défaillant, voir contraintes).

Sortantes :

- API isochrone IGN v2 (isochrones pédestres depuis chaque entrée de parc) ;
- Copernicus CDSE (NDVI par parc, quota gratuit 10 000 requêtes/mois) ;
- adresse.data.gouv.fr (localisation par adresse) ;
- Mailjet (emails, flag `application.sendEmail`, actuellement désactivé) ;
- Matomo (optionnel) ;
- Publication Git : écriture des fichiers sous `application.admin.export-data-path` puis commit/push JGit vers le dépôt GitHub Pages.

Règles par contrôleur : [docs/workflow-rules/README.md](https://github.com/cunvoas/opendata-tools/tree/main/geoservice-isochrone-v2/docs/workflow-rules).

## Core Domain Concepts

| Concept | Sens / usage | Propriété |
| --- | --- | --- |
| ParcEtJardin | Polygone d'un parc/jardin, chargé des données d'une collectivité ou créé par un contributeur | Produit |
| Entrée | Point d'accès d'un parc ; les isochrones sont calculés par entrée puis fusionnés | Produit |
| Carreau INSEE 200 m | Maille de 200 m × 200 m avec population Filosofil ; unité principale de mesure de l'accessibilité | Consommé (INSEE), valeurs calculées produites |
| IRIS | District statistique INSEE, deuxième maillage de calcul | Consommé (INSEE), valeurs calculées produites |
| Indicateurs OMS | Seuils configurés : zone urbaine = 300 s à pied, 12 m²/hab recommandés, 10 m²/hab minimum ; périurbain = 1200 s, 45/25 m²/hab ; « parc durable » à ≥ 5 000 m² à 300 m | Consommé (OMS), production configurée |
| ParkAreaComputed | Métriques par parc et par année (surface, population, OMS, NDVI moyenne/min/max/écart-type, date d'acquisition) | Produit |
| Proposition (auto ou manuelle) | Aménagement proposé par un des 10 algorithmes ou dessiné à la main (cercle/polygone), avec évaluation d'impact OMS persistée (`ManualEvalWork`) | Produit (jamais appliqué automatiquement aux parcs réels) |
| Publication | Export statique par EPCI et année, poussé vers le dépôt GitHub Pages | Produit |
| Contributeur | Compte utilisateur à rôles, mot de passe Argon2 | Produit |

## Main Workflows

### Calcul (carreau 200 m + IRIS)

1. Mise à jour d'un parc ou demande admin : `BatchJobService` crée des jobs de calcul (`compute_job`, `compute_iris_job`), réutilisés/recyclés quand une mise à jour plus récente existe.
2. Un planificateur `@Scheduled` (toutes les 300 s, un seul thread) traite les jobs en attente. Un seuil de température CPU (fourni par une URL de métriques externe) ralentit le traitement si la machine chauffe.
3. Par job : le service cherche les parcs dont l'isochrone (IGN) atteint le carreau depuis leurs entrées, calcule surface/habitant, indicateurs OMS et déficits, et enregistre dans PostGIS.
4. Les jobs en erreur ou restés « en cours » sont recyclés au prochain passage. L'arrêt est gracieux (grace period 60 s au redémarrage).

Effets : uniquement des lignes PostGIS mises à jour. Échec : job en `IN_ERROR`, rejoué automatiquement ; aucune donnée externe modifiée. Preuves : code `BatchJobService`, [BatchJobService.md](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/docs/BatchJobService.md), [ComputeCarreServiceV4.md](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/docs/ComputeCarreServiceV4.md).

### NDVI par parc (végétation satellite)

1. Pendant le calcul OMS d'un parc, si `application.ndvi.enabled=true`, le service interroge l'API statistique de Copernicus (Sentinel-2 L2A, mosaïque la moins nuageuse, nuages/ombres/eau masqués). Aucune image n'est téléchargée : le calcul se fait côté fournisseur, seules quelques centaines d'octets reviennent.
2. Fenêtre glissante sur l'année précédente (configurable en MM-dd), avec fenêtre de secours si la première ne donne aucune donnée exploitable.
3. Au plus **un appel par parc et par an**, uniquement à partir de septembre (date mémorisée en base `ndvi_request_date`).
4. Toute erreur (réseau, quota, timeout) est non bloquante : colonnes NDVI nulles + log WARN, le calcul OMS se termine quand même.
5. Le quota mensuel est persisté en base (migration `v2.1.4_ndvi_api_quota.sql`) : réservation atomique avant l'appel, remboursée si la réponse n'est pas OK ; débit plafonné à 2 req/s avec reprise sur erreur 429.

Effets : colonnes NDVI de `ParkAreaComputed` alimentées ou nulles ; quota préservé. Désactivé par défaut (`ndvi.enabled: false`) tant que les identifiants CDSE ne sont pas configurés. Preuves : README du composant, [copernicus-ndvi-oauth.md](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/docs/copernicus-ndvi-oauth.md), migrations `v2.1.3/4/5_ndvi_*.sql`.

### Propositions d'aménagement (auto ou manuelles)

1. Le contributeur choisit un des 10 algorithmes (itératif ×4, solveur de contraintes PPC ×3, χ² avec voisins ×2, génétique) qui propose où créer ou agrandir un parc pour respecter l'OMS.
2. Les propositions manuelles (cercle ou polygone) sont dessinées dans l'interface et évaluées contre les indicateurs OMS ; l'impact est persisté (`ManualEvalWork`).

Effets : base de données uniquement. Les propositions ne sont **pas** appliquées automatiquement aux parcs réels — elles servent d'aide à la décision pour la collectivité. Preuves : `service/solver/`, `service/proposal/`, docs `*-strategy.md`.

### Publication

1. Depuis l'admin : `POST /publish/api/request` vérifie d'abord que **tous les calculs de l'EPCI sont terminés sans erreur** (`canPublish`), puis passe en asynchrone (réponse `202`).
2. Phase **production** : écriture des GeoJSON (référentiels, contours de parcs, isochrones, carreaux, IRIS) et statistiques (par ville, zones denses, périurbaines, globales) sous le répertoire d'export. Toute erreur arrête la phase et **aucun push Git n'est tenté**.
3. Phase **publication Git** (uniquement si production OK) : un commit unique, push vers le dépôt GitHub Pages ; statuts terminaux `PUBLISHED` (SHA du commit exposé), `PUSH_FAILED` ou `PUSH_NOT_CONFIGURED`.
4. L'interface admin interroge le statut (`/publish/api/status`).

Effets : fichiers sur disque + commit Git public. Échec : visible dans le statut, pas de données partielles publiées (la production doit être entièrement propre avant push). Preuves : [publication.md](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/docs/publication.md), code `PublishRestControler`/`PublishService`.

## Configuration and Dependencies

- Runtime : Java 21, Spring Boot 4.1.1, Spring Security, Hibernate Spatial, Thymeleaf, Micrometer/Prometheus.
- Clés de configuration importantes dans [application.yml](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/src/main/resources/application.yml) : `application.business.oms.*` (seuils OMS), `application.business.insee.densite` (1,2 = communes denses), `annees: 2015,2017,2019,2021` (population Filosofil), NDVI (`enabled: false`, quota 10 000/mois, fenêtre juin), sélecteurs d'implémentation (`isochrone-impl`, `carre200m-impl`).
- Secrets de dev : `application-dev.yml` contient encore des valeurs inline en clair (mot de passe DB, secret JWT, clé d'export, client-secret Git) — vérifiées présentes au commit validé ; à purger de l'historique Git. Aucune valeur reproduite ici.
- Manifeste complet : [pom.xml](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/pom.xml).

## Operational Characteristics

- Déploiement : image Docker (non-root), nœud unique ; planificateur volontairement mono-thread (calculs sérialisés pour éviter la surcharge CPU et le dépassement des quotas externes).
- Observabilité : endpoints actuator limités, Prometheus/metrics, appender Loki, Zipkin optionnel.
- Réessai : les jobs en erreur ou bloqués en « en cours » sont recyclés au prochain passage (300 s), sans backoff exponentiel.
- Arrêt : gracieux, 60 s de marge ; un calcul géospatial en cours est recyclé au redémarrage suivant.
- Migrations Flyway automatiques au démarrage (`clean-disabled` : jamais de destruction automatique de la base).

## Constraints and Non-goals

### Verified Constraints

- Années de population disponibles uniquement : **2015, 2017, 2019, 2021** (Filosofil). Pas d'année plus récente sans import INSEE correspondant.
- Maillages : carreau INSEE 200 m et IRIS — pas de calcul à la parcelle ou à l'adresse.
- Garde de publication : `canPublish` exige qu'aucun job de calcul de l'EPCI ne soit en erreur ou en attente — une publication sur données incomplètes est refusée, pas corrigée.
- Rôles : consultation anonyme (lecture seule), contributeur (écriture des parcs), administrateur (calculs d'une ville/EPCI, association ville↔EPCI, publication).
- Le site public consomme des **fichiers statiques figés** au moment de la publication : si le dossier export est supprimé, le flux public est coupé jusqu'à la prochaine publication.
- Historique qualité : sur la MEL, les données sources contenaient des erreurs, corrigées par requalification (pas de suppressions) après études contradictoires (Préfecture du Nord). Toute réutilisation presse doit vérifier l'année de référence et la date de dernière mise à jour des données publiées.

### Non-goals

- Produire les données INSEE/IGN/OSM sources (les portails restent la référence).
- Héberger le site public et son contenu statique (dépôt GitHub Pages séparé).
- Délivrabilité email, stockage analytics, hébergement de fond de carte : délégué à Matomo/Mailjet/fournisseurs de tuiles.
- Temps réel : tout est calculé par campagne annuelle ; NDVI rafraîchi au maximum une fois par an par parc.

## Current and Planned Behavior

### Implemented (vérifié sur `main` au commit validé)

- Pipeline de calcul par carreau 200 m et IRIS : file de jobs, recyclage, limitation thermique CPU, arrêt gracieux ; moteur de calcul **V4** optimisé (batch population, cache surfaces, mutualisation result/OMS).
- Client API isochrone IGN v2 (implémentation commutable), géocodage adresse.
- **NDVI Copernicus par parc** : client API statistique, colonnes NDVI + quota persisté (migrations v2.1.3 à v2.1.5), déclenchement non bloquant, règle « un appel par parc et par an ». Feature flag `ndvi.enabled: false` par défaut.
- **Publication Git intégrée** : production 100 % propre avant push, phases tracées (`PRODUCING`/`PRODUCED`/`PUBLISHING`/`PUBLISHED`/`PUSH_*`), SHA de commit exposé dans le statut.
- Suite de solveurs : **10 algorithmes** (`ProposalComputationTypeAlgo`) + propositions manuelles (cercle/polygone) avec évaluation d'impact OMS.
- Gestion des contributeurs : rôles, Argon2, limitation des tentatives de connexion.
- Procédure de mise à jour INSEE/Overpass depuis le cadastre documentée ([maj_insee_overpass_depuis_cadastre.md](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/docs/maj_insee_overpass_depuis_cadastre.md)).

### Planned (cadrages / framings, non livrés)

- **Restitution NDVI côté front** ([ndvi-parc-restitution-framing.md](https://github.com/cunvoas/opendata-tools/blob/main/framing-documents/ndvi-parc-restitution-framing.md), T4 2026, cadrage en cours) : écran récapitulatif `/ndvi` (table + distribution), exposition REST `/ndvi/summaries`, mode colorimétrique NDVI sur la couche Parcs, NDVI au clic dans le panneau de détail, règle « un appel par parc sur l'année de référence seulement » (791 appels/an pour la MEL au lieu de 4 appels possibles par parc aujourd'hui). Faits mesurés au 2026-10-26 sur base de dév : 230 parcs avec NDVI sur 791 attendus (2021), NDVI moyen 0.403, forte hétérogénéité intra-parc (Parc de la Citadelle : moyenne 0.593, écart-type 0.300).
- **WMS raster Sentinel-2 sur la carte** ([recuperation-ndvi-parc-framing.md](https://github.com/cunvoas/opendata-tools/blob/main/framing-documents/recuperation-ndvi-parc-framing.md)) : calque « Végétation réelle (NDVI Satellite) », front-only, désactivé par défaut ; explicitement reporté tant que la restitution par parc n'est pas livrée.
- **Intégration Overpass au calcul** : parseurs présents dans le code, mais le raccord REST des jobs reste un stub (marqueur `TODO OVERPASS` dans `ComputeJobRestControler`) — les parcs OSM ne sont pas encore calculés via ce chemin.
- **Publication automatique GeoJSON des EPCI** ([publication-geojson-github-epci-framing.md](https://github.com/cunvoas/opendata-tools/blob/main/framing-documents/publication-geojson-github-epci-framing.md)) : le câblage Git ciblé par ce cadrage est désormais implémenté (phases `PUBLISHING`), mais le cadrage reste la référence d'intention et de garde-fous (publication volontaire, jamais déclenchée par les imports batch).
- « Colonne seule » ParkAreaComputed NDVI ([ndvi-par-parc-park-area-computed-framing.md](https://github.com/cunvoas/opendata-tools/blob/main/framing-documents/ndvi-par-parc-park-area-computed-framing.md)) : les colonnes et le pipeline prévus par ce cadrage sont désormais implémentés.

### Unknown or To Verify

- Comportement du push Git JGit en production (identifiants `application.git.*`) : présent et tracé en code, non confirmé au-delà de la doc ; vérifier les statuts `PUSH_*` en cas de rotation de token GitHub.
- NDVI désactivé en prod tant que les identifiants CDSE ne sont pas configurés : confirmer l'état réel du déploiement (le flag est `false` par défaut dans le dépôt).
- Le raccord Overpass dans le pipeline de calcul reste en stub : la couverture des parcs français issue d'OpenStreetMap dépend de l'avancement de ce chantier.
- Le suivi dans le temps des propositions (`ManualEvalWork`) : usage réel non attesté, à confirmer avec le mainteneur.

## Related Agent Instructions

| Instruction | Périmètre | But | Statut |
| --- | --- | --- | --- |
| [docs/workflow-rules/README.md](https://github.com/cunvoas/opendata-tools/tree/main/geoservice-isochrone-v2/docs/workflow-rules) | workflow-rules | Index des règles architecturales par contrôleur/service (18 éléments) | Accepted |
| [BatchJobService.md](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/docs/BatchJobService.md) | File de jobs | Cycle 300 s, recyclage, surchauffe CPU | Accepted |
| [ComputeCarreServiceV4.md](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/docs/ComputeCarreServiceV4.md) | Moteur de calcul | Contrat de calcul par carreau, versions v2/v3/v4 | Accepted |
| [publication.md](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/docs/publication.md) | Publication | Phases production → push Git, garde `canPublish` | Accepted |
| docs `*-strategy.md` (carre-recherche, iterative2, least-squares-neighbour2, genetic1, proposal-computation, solver3-zones-ignorees-fix) | Solveurs | Détail algorithme par algorithme | Accepted |
| [copernicus-ndvi-oauth.md](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/docs/copernicus-ndvi-oauth.md) | NDVI | OAuth CDSE, configuration et dépannage | Accepted |
| [maj_insee_overpass_depuis_cadastre.md](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/docs/maj_insee_overpass_depuis_cadastre.md) (+ `maj_insee_overpass_update.sql`) | Mise à jour | Procédure de mise à jour depuis le cadastre | Accepted |
| README.md du composant | Sources, NDVI, quotas CDSE, OMS | Vue d'ensemble opérationnelle | Accepted |
| [framings](https://github.com/cunvoas/opendata-tools/tree/main/framing-documents) (×4) | Restitution NDVI, WMS, publication EPCI, colonne ParkAreaComputed | Cadrages produit (T4 2026) | Draft/proposal |
| [TESTING_GUIDE.md](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/TESTING_GUIDE.md), [QUICK_START_TESTS.md](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/QUICK_START_TESTS.md) | Tests | Lancer les tests unitaires/intégration | Accepted |
| [SECURITY.md](https://github.com/cunvoas/opendata-tools/blob/main/SECURITY.md) (racine) | Politique de sécurité | Versions supportées, signalement de vulnérabilité | Accepted |

## Authoritative References

- [Dépôt](https://github.com/cunvoas/opendata-tools) — composant sous `geoservice-isochrone-v2/`
- [Point d'entrée](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/src/main/java/com/github/cunvoas/geoserviceisochrone/GeoserviceIsochroneApplication.java)
- [README du composant](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/README.md)
- [Index des règles par contrôleur](https://github.com/cunvoas/opendata-tools/tree/main/geoservice-isochrone-v2/docs/workflow-rules)
- [Cadrages (framings)](https://github.com/cunvoas/opendata-tools/tree/main/framing-documents)
- [Documentation technique](https://github.com/cunvoas/opendata-tools/tree/main/geoservice-isochrone-v2/docs) (BatchJobService, ComputeCarreServiceV4, publication, NDVI, solveurs)
- [Configuration (application.yml)](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/src/main/resources/application.yml)
- [Migrations Flyway](https://github.com/cunvoas/opendata-tools/tree/main/geoservice-isochrone-v2/src/main/resources/db/migration)
- [Politique de sécurité (SECURITY.md)](https://github.com/cunvoas/opendata-tools/blob/main/SECURITY.md)
- [Tests](https://github.com/cunvoas/opendata-tools/tree/main/geoservice-isochrone-v2/src/test)
- [Manifeste (pom.xml)](https://github.com/cunvoas/opendata-tools/blob/main/geoservice-isochrone-v2/pom.xml)
- Site public des données publiées : [parcs-et-jardins](https://autmel.github.io/parcs-et-jardins) (dépôt GitHub Pages alimenté par la publication)
- Référence OMS citée par le projet : [Urbanisme et santé (OMS/Europe)](https://www.euro.who.int/__data/assets/pdf_file/0010/102106/E93982.pdf)
