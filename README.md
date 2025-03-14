[![Known Vulnerabilities](https://snyk.io/test/github/dwyl/hapi-auth-jwt2/badge.svg?targetFile=package.json)](https://snyk.io/test/github/dwyl/hapi-auth-jwt2?targetFile=package.json)
[![DeepSource](https://deepsource.io/gh/cunvoas/opendata-tools.svg/?label=active)](https://deepsource.io/gh/cunvoas/opendata-tools/?ref=repository-badge)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/0a8911c0ee04464b83490f95d04c84cf)](https://app.codacy.com/gh/cunvoas/opendata-tools/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)

# Résumé

Ce projet a pour but premier de quantifier, avec des données fiables et des méthodes de calcul publiques, l'état des parcs et jardins pour les habitants de la MEL.

Dans le cadre de l'analyse des données. Il s'est avéré que les données fournies par la MEL sont erronées et des études contradictoires, notament de la Préfecture du Nord et de la Direction du territoire, nous ont permis de requalifier certaines données, sans pour autant les effacer.


## opendata-tools
la racine est un projet parent qui englobe les sous-projets et outillages requis dans la construction et l'alimentation des données.


## dbf2csv
Sur les plateformes opendata, il y a souvent des fichiers de la base de données [DBase](https://www.dbase.com/). Ce programme permet de convertir les fichiers DBF en CSV.
[Détail du projet](./dbf2csv/README.md)

## geoservice-isochrone
Projet de traitement et données, contenant les calculs, les services web et la base de données.
[Détail du projet](./geoservice-isochrone/README.md)

## geoservice-map-vue3
Projet de l'interface graphique qui représente les données sous la forme de cartes.
[Détail du projet](./geoservice-map-vue3/README.md)


## opendata-tool-spare-data

Ce projet contient un copie de sécurité des fichiers de données sources afin de pouvoir reconstruire la base de données.
Cette copie a été décidée car les données du passé deviennent souvent indisponibles ou le format des données évoluent dans le temps. 
[Détail du projet](https://github.com/cunvoas/opendata-tool-spare-data/README.md)

