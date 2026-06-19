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

Le service de publication réalise les étapes suivantes :

1. **Export GeoJSON** : Génération des fichiers de carreaux (200m) pour la zone sélectionnée.
2. **Calcul de statistiques** :
   - Statistiques pour les zones denses (urbaines).
   - Statistiques pour les zones péri-urbaines (suburbs).
   - Statistiques globales consolidées.

## Emplacement des fichiers

Les fichiers générés sont stockés selon la configuration définie dans `ApplicationBusinessProperties`. Par défaut, ils se trouvent dans le dossier `jsonFileFolder` structuré comme suit :
- `/geojson/carres/{com2coId}/carre_{annee}_{com2coId}.json`
- `/data/stats/com2co/{com2coId}/stats_c2c_{com2coId}_..._{annee}.json`
