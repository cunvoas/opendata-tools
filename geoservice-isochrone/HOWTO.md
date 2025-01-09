# PostgreSQL + Postgis

La solution utilise [PostgreSQL 15.2](https://www.postgresql.org/docs/) avec l'extention [postgis 3.3.3](https://postgis.net/docs/).
Ce document explique rapidement comment [installer la base de données](https://houarinourreddine.medium.com/integrate-spring-boot-and-postgis-to-manage-spatial-data-272edacf2cb).

La base avec les données carroyées, le cadatre des villes et les parcs fait 3 Go environ. L'application backoffice 55 Mo et l'application front 3 Mo.

# Java, SpringBoot, Maven

Le back est codé en Java 19 (java 17 minimum) sur une openjdk. 
Maven 3.9 est utilisé pour la gestion des dépendances et le build. 
Spring Boot 3.1 est utilisé comme framework structurant.

# VueJS, Leaflet

L'IHM de la carte publique est réalisée en VueJS 3 avec vue3-leaflet et leaflet pour la carte. La carte communique avec les données précalculées de l'app de gestion et GeoJSON.

# Thymleaf, Leaflet
L'IHM de l'application de gestion est réalisée avec Thymleaf et Leaflet pour les cartes. La carte communique avec le serveur en ReST et GeoJSON.

# Docker

Docker est requis pour une installation simplifiée avec des conteneurs.
L'application est suffisement légère pour fonctionner sur Raspberry Pi 4. Attention cependant à l'espace de stockage de le base de données car les données Françaises (INSEE, La poste) sont chargées en intégralité, la base représente environ 3 Go de données.

@@TODO bientot







