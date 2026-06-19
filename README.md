[![Known Vulnerabilities](https://snyk.io/test/github/cunvoas/opendata-tools/badge.svg)](https://snyk.io/test/github/cunvoas/opendata-tools?targetFile=geoservice-isochrone-v2/pom.xml)
[![DeepSource](https://deepsource.io/gh/cunvoas/opendata-tools.svg/?label=active)](https://deepsource.io/gh/cunvoas/opendata-tools/?ref=repository-badge)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/0a8911c0ee04464b83490f95d04c84cf)](https://app.codacy.com/gh/cunvoas/opendata-tools/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)

# Résumé

L'outil a été conçu pour un usage **national** — toutes les EPCI de France sont référencées et peuvent être analysées.

La saisie des parcs et jardins est participative : les habitants et associations locales contribuent à renseigner les espaces verts de leur territoire. Cela permet d'obtenir un état des lieux fiable et partagé, quel que soit le niveau d'ouverture des données de leur collectivité.

L'étude a débuté sur la MEL (Métropole Européenne de Lille), où les données fournies contenaient des erreurs. Des études contradictoires (Préfecture du Nord, Direction du Territoire) ont permis de requalifier certaines données, sans les effacer.

Le projet est déployé sur [autmel.github.io/parcs-et-jardins](https://autmel.github.io/parcs-et-jardins).


## opendata-tools

Racine parente englobant les sous-projets et outillages pour la construction et l'alimentation des données.

| Sous-projet | Rôle |
|---|---|
| [geoservice-isochrone-v2](./geoservice-isochrone-v2/README.md) | Backend Spring Boot 4 — calculs isochrones, batchs, API REST, base PostGIS |
| [geoservice-map-vue3](./geoservice-map-vue3/README.md) | Frontend Vue 3 + Leaflet — carte interactive des parcs et données |
| [dbf2csv](./dbf2csv/README.md) | Convertisseur DBF → CSV pour données OpenData |
| [docker](./docker/) | Images et configs Docker (Java bellsoft/distroless/pilotfish, reverse proxy) |
| [opendata-tool-spare-data](https://github.com/cunvoas/opendata-tool-spare-data/) | Sauvegarde des données sources brutes (pérennité) |

## Fonctionnalités principales

- **Calcul d'isochrones piétons** : distance d'accès aux parcs selon la densité (300m dense, 1200m périurbain)
- **Indicateurs OMS** : m²/hab, surfaces manquantes, parcs durables (≥5000m² à 300m)
- **Grille INSEE 200m + IRIS** : carroyage fin et maillage IRIS pour le calcul des métriques
- **Données Filosofil** : population carroyée 2015, 2017, 2019, 2021
- **Moteur de calcul V4** : optimisé (batch population, cache surfaces, mutualisation result/resultOms)
- **File d'attente asynchrone** : jobs gérés par `@Scheduled`, recyclage automatique des erreurs
- **Publication GeoJSON** : export de données statiques pour le frontend
- **Moteur de propositions** : algorithmes génétique, moindres carrés (χ²), programmation par contrainte, itératif
- **Simulation de projets** : évaluation de scénarios d'aménagement avec code couleur
- **Export PDF** des simulations
- **PWA** sur la carte publique
- **Gestion des utilisateurs, rôles, mailing**
- **Monitoring APM** et **Matomo Analytics**

## Stack technique

| Couche | Technologie |
|---|---|
| Backend | Java 21, Spring Boot 4, Hibernate 7, Jackson 3 |
| Base de données | PostgreSQL + PostGIS |
| Frontend | Vue 3, Vite, Leaflet, Tailwind CSS |
| Géomatique | IGN API, OpenStreetMap, LocationTech JTS |
| Infrastructure | Docker (multi-images), Reverse Proxy |
| CI/Qualité | Codacy, DeepSource, Snyk, GitHub Advanced Security |

## Versions

Le projet est en **V2.0.5** — voir [CHANGELOG](./CHANGELOG.md) et [RELEASE](./RELEASE.md).

