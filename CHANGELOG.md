# Changelog



## V2.0.0 - 2026-05-02

 - Migration technique
    - Spring Boot 4
    - Hibernate 7
    - Jackson 3

## V1.1.8 - 2026-04-22

- Fix sécurité critique

## V1.1.7 - 2026-03-27

- Migration Monitoring APM

## V1.1.6 - 2026-03-25

- Fix: security advices after audit
- Algos de proposition:
- Itératifs
- Programmation Par Contrainte (PPC)
- χ²
- Génétique

## V1.1.5 - 2026-03-12

- Simulation avec code couleur.

## V1.1.4 - 2026-02-23

- Amélioration Simulation.

## V1.1.3 - 2026-02-15

- Ajout de Matomo.

## V1.1.2 - 2026-01-31

- Ajout de PWA sur la carte publique.
- Présentation des propositions.

## V1.1.1 - 2026-01-21

- Ajout d'une date de connnexion pour audit.
- Moteur de proposition PPC.

## V1.1.0 - 2026-01-11

- Amélioration de l'UX des pages simples.
- Export PDF des Simulations.
- Moteur de proposition en brute force.

## V1.0.28 - 2025-12-15

- Simulation de projet.

## V1.0.27 - 2025-12-15

- Ajout de couleur de status de parcs sur la page de consultation.

## V1.0.26 - 2025-12-11

- Première proposition de parcs (algo par priorisation des déficits).
- Refonte graphique du site publique (tailwind).

## V1.0.25 - 2025-11-22

- Gestion des groupements de communes.
- Lancements des calculs pour une communauté de communes.
- Page de consultation des données non publiées au public.

## V1.0.24 - 2025-11-19

- Site publique v2
- Sélection des résultats pour les stats/ville.
- Demande de calculs depuis l'interface de gestion.

## V1.0.23 - 2025-10-19

- Génération des statistiques

## V1.0.22 - 2025-10-26

- Fix anomalies de saisies de parcs

## V1.0.21 - 2025-10-19

- Protection anti-rejeux
- Fix sécurité: diverses librairies

## V1.0.20 - 2025-04-16

- Videos d'aide à la saisie
- Service de calcul v3
- Methodes d'export des GeoJsons publique
- Préparation des travaux pour les IRIS
- Fix sécurité: diverses librairies
- Documentation

## V1.0.19 - 2025-03-11

- Fix: security advices after audit

## V1.0.18 - 2025-03-07

- Monitoring APM
- Ajout d'une liste de calcul des carreaux

## V1.0.17 - 2025-02-19

- Fix: après création de parc, on va en mode modification sur le nouveau parc
- Fix: création d'un contour adjacent à un contour existant
- Ajout de code couleur sur les parcs avec la légende
- Ajout de liens pour accès rapide parc ↔ entrées

## V1.0.16 - 2025-02-06

- Ajout du monitoring de l'application
- Amélioration de l'algorithme de fusion des isochrones

## V1.0.15 - 2025-01-29

- Fix: optimisation, relocalisation automatique des parcs sur la ville

## V1.0.14 - 2025-01-14

- Fix: edition de contours de parc de la couche autmel

## V1.0.13 - 2025-01-06

- Fix: revue du pool du SGBDR

## V1.0.12 - 2025-01-06

- Correctif: les parcs sans contours apparaissent avec un marqueur si les coordonnées existent.
- Montée de version des librairies dépendantes

## V1.0.11 - 2024-12-31

- Sélecteur de région, ville et d'adresse sur la carte publique
- Refonte du moteur des couleurs sur la carte publique (optimisation et réduction des tailles)
- Publication des cartes pour l'administateur.
- Intégration du critère 0,5 ha à proximité.
- Corrections: timehack, rafraichissement des parcs dans les formulaires de saisie
- Ajouter les parcs de l'OpenData Lyon, Nantes & Toulouse

## V1.0.10 - 2024-12-05

- Création de parc
- Modification du sélecteur de ville
- Page de login custom
- Homogénéisation des boutons.
- Migration technique de la carte publique de vue2 à vue3
- Démo de statistiques d'accès aux parcs

## V1.0.9 - 2024-07-17

- Carreaux de 200m au national
- Donnée carroyées de 2015, 2017, 2019
- Données de villes et des références de densité
- Intégration des nouvelles API de l'IGN
- Refonte du moteur de calcul et optimisations
- Nouveau rendu de la carte publique
- Publication de la carte publique pré-calculée

## V1.0.8 - 2024-03-10

- Info parc, surface, m²/hab, type, status
- Ajouter un typage pour les parcs (parc, bosquet, cimetière).
- Ajouter un status pour les parcs (fait, a faire, a recalculer).

## V1.0.7 - 2024-02-29

- FIX Sécurité CVE-2024-25710, CVE-2024-1597
- Correction des stats sur les isochrones

## V1.0.6 - 2024-02-16

- Rendre disponible depuis l'interface la fusion des isochrones des entrées
- Lancer depuis l'ihm la fusion des isochrones des entrées de parc.

## V1.0.5 - 2024-02-05

- Réactivation de l'envoi de mail.
- Possibilité de régénérer un mot de passe.
- Page d'information CNIL et sécurité.
- Affichage des isochrones de chaque entrée sur le formulaire de gestion des entrées

## V1.0.4 - 2024-02-02

- Désactivation de l'envoi de mail.

## V1.0.3 - 2024-01-30

- Ajouter les parcs de l'OpenData Roubaix & Tourcoing
- Automatiser l'envoi de mail en cas de création ou de réinitialisation de mot de passe.

## V1.0.2 - 2024-01-25

- Liste et édition des associations
- Liste et édition des utilisateurs

## V1.0.1 - 2024-01-19

- Edition des entrées
- Liste des parcs

## V1.0.0 - 2023-10-10

- Première version

## V0.1.0 - 2023-06-15

- MVP Lille-Hellemmes-Lomme publique

## V0.0.1 - 2023-03-28

- MVP privé Lille-Hellemmes-Lomme
- Moteur geojson
- Saisie GSheet
