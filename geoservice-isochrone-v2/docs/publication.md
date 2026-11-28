# Publication des données

Cette documentation décrit le processus de publication des données géographiques et des statistiques au sein du géo-service.

## Vue d'ensemble

Le processus de publication permet de générer des fichiers statiques (GeoJSON) et des indicateurs de performance (statistiques de surface) pour une communauté de communes et une année données. Ces fichiers sont destinés à être consommés par le front-end ou par des services tiers.

## Fonctionnement asynchrone

Pour garantir une réactivité optimale de l'interface d'administration, la publication est effectuée de manière **asynchrone**. 

1. L'utilisateur déclenche la production via l'interface (bouton « Publier » de la fiche EPCI ou point d'entrée REST `/mvc/ajax/publish/api/request`).
2. Le `PublishRestControler` vérifie les droits et l'existence des données.
3. Si les conditions sont remplies, il délègue le travail au `PublishService.publishAsync()`.
4. Une réponse `202 ACCEPTED` est immédiatement renvoyée au client.
5. Le `PublishService` exécute les tâches lourdes en arrière-plan.

## Tâches effectuées

Le service de production réalise les étapes suivantes :

1. **Référentiels** (`writeRegions`) : `data/regions.json`, EPCI par région `data/com2cos/{regionId}/com2cos_{regionId}.json`, villes par EPCI `data/cities/com2co/cities_{com2coId}.json` et densité `data/cities/densite.json`.
2. **Statistiques** : par ville (`statsSurfaceService.writeStatsSurfaceByCom2CoIdAndAnnee`), zones denses (urbaines), zones péri-urbaines (suburbs), globales consolidées.
3. **Données géographiques de l'EPCI et de l'année** : `parkOutline`, `isochrone`, `carreaux`, `iris`, puis NDVI EPCI et villes.
4. **Index des territoires** (`writeTerritoires`) : `data/territoires.json`, classé avec le référentiel dans le commit final.
5. **Aucun push Git** : la production s'arrête après l'écriture des fichiers.

`data/territoires.json` dépend des statistiques : il est écrit après elles, mais il fait partie du lot référentiel du dashboard et du commit.

## Statut de production et commit final

La production est asynchrone et expose une progression détaillée via `POST /mvc/ajax/publish/api/status` :

- phase (`PRODUCING`, `PRODUCTION_FAILED`, `PRODUCED`) ;
- étape courante (`x/y`), libellé et état de chaque étape ;
- compteur de villes (`x/y`) pour les statistiques ;
- nombre d'objets et fichiers produits / attendus ;
- durée écoulée et ETA approximative.

Une étape en échec ou un fichier attendu absent bloque la production : la phase passe à `PRODUCTION_FAILED` et aucun commit n'est possible.

Une fois la production terminée (`PRODUCED`), l'administrateur ou le gestionnaire d'association ouvre le **dashboard d'état des lieux** (`GET /mvc/management/exports`). Pour un gestionnaire (`ASSO_MANAGER`), la vue est limitée à son EPCI (`idCommunauteCommune`) : matrice, fichiers et commit ne portent que sur cet EPCI. Pour un administrateur, la vue est nationale et paginée (10, 50 ou 100 EPCI par page). L'administrateur vérifie la matrice EPCI/année, les fichiers nouveaux/modifiés/manquants et les anomalies, puis déclenche le **commit final unique** via `POST /mvc/ajax/publish/api/commit` avec le hash du snapshot affiché. Le dashboard stage une liste exacte de fichiers (jamais `git add -A`), crée un seul commit et pousse si la configuration Git est présente. Il retourne un résultat avec le SHA, l'état du push (`pushed`) et un message (push OK, push en échec, push non configuré).

La configuration Git attendue (`application.git.*`) est : `localPath` (clone local, défaut = `application.admin.export-data-path`), `username`, `token`.

## Emplacement des fichiers

Les fichiers générés sont stockés selon la configuration définie dans `ApplicationBusinessProperties`. Par défaut, ils se trouvent dans le dossier `jsonFileFolder` structuré comme suit :
- `/geojson/carres/{com2coId}/carre_{annee}_{com2coId}.json`
- `/data/stats/com2co/{com2coId}/stats_c2c_{com2coId}_..._{annee}.json`
