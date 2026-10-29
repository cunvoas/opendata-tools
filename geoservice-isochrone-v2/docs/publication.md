# Publication des données

Cette documentation décrit le processus de publication des données géographiques et des statistiques au sein du géo-service.

## Vue d'ensemble

Le processus de publication permet de générer des fichiers statiques (GeoJSON) et des indicateurs de performance (statistiques de surface) pour une communauté de communes et une année données. Ces fichiers sont destinés à être consommés par le front-end ou par des services tiers.

## Fonctionnement asynchrone

Pour garantir une réactivité optimale de l'interface d'administration, la publication est effectuée de manière **asynchrone**. 

1. L'utilisateur déclenche la publication via l'interface (bouton "Calculer" ou point d'entrée REST `/mvc/ajax/publish/api/request`).
2. Le `PublishRestControler` vérifie les droits et l'existence des données.
3. Si les conditions sont remplies, il délègue le travail au `PublishService.publishAsync()`.
4. Une réponse `202 ACCEPTED` est immédiatement renvoyée au client.
5. Le `PublishService` exécute les tâches lourdes en arrière-plan.

## Tâches effectuées

Le service de publication réalise les étapes suivantes (séquence de référence : `TestExporterApplication#export`) :

1. **Référentiels** (`writeRegions`) : `data/regions.json`, EPCI par région `data/com2cos/{regionId}/com2cos_{regionId}.json`, villes par EPCI `data/cities/com2co/cities_{com2coId}.json` et densité `data/cities/densite.json`.
2. **Données géographiques de l'EPCI et de l'année** : `parkOutline`, `isochrone`, `carreaux`, `iris`.
3. **Statistiques de l'EPCI** :
   - Statistiques par ville (`writeStatsSurfaceByCom2CoIdAndAnnee`).
   - Statistiques pour les zones denses (urbaines).
   - Statistiques pour les zones péri-urbaines (suburbs).
   - Statistiques globales consolidées.
4. **Publication Git** : commit et push des fichiers produits vers le dépôt GitHub Pages.

## Statut de production et publication Git

La publication se déroule en deux phases distinctes, exposées par `POST /mvc/ajax/publish/api/status` (champ `publication`) :

1. **Production** (`PRODUCING`) : chaque fichier produit est enregistré (type, chemin, taille, date, succès/échec). Si une étape échoue, la phase passe à `PRODUCTION_FAILED` et **aucun push Git n'est tenté**.
2. **Publication Git** (`PUBLISHING`) : uniquement après une production terminée sans erreur (`PRODUCED`). Un seul commit contient les référentiels (`data/regions.json`, `data/com2cos`, `data/cities`) puis les données de l'EPCI : `geojson/parkOutline/{id}`, `geojson/isochrones/{id}`, `geojson/carres/{id}`, `geojson/iris/{id}`, `data/stats/com2co/{id}` et les dossiers de stats par ville `data/stats/{dept}/{insee}`.

Phases terminales : `PUBLISHED` (SHA du commit exposé), `PUSH_FAILED` (fichiers conservés, statut porte l'erreur), `PUSH_NOT_CONFIGURED` (fichiers produits, credentials Git absents).

La configuration Git attendue (`application.git.*`) est : `localPath` (clone local, défaut = `application.admin.export-data-path`), `username`, `token`.

## Emplacement des fichiers

Les fichiers générés sont stockés selon la configuration définie dans `ApplicationBusinessProperties`. Par défaut, ils se trouvent dans le dossier `jsonFileFolder` structuré comme suit :
- `/geojson/carres/{com2coId}/carre_{annee}_{com2coId}.json`
- `/data/stats/com2co/{com2coId}/stats_c2c_{com2coId}_..._{annee}.json`
