Futures fonctionnalités (à prioriser)

    Intégration de la richesse des foyers dans les stats.
    Sélection des résultats pour les stats/ville.
    Ajout des photos sur le site publique.
    Gestion des groupements de communes.
    Publication des cartes depuis l'interface de gestion.
    Refonte graphique du site publique (tailwind)?



V1.0.17-20250219

    fix: après création de parc, on va en mode modification sur le nouveau parc
    fix: création d'un contour adjacent à un contour existant
    Ajout de code couleur sur les parcs avec la légende
    Ajout de liens pour accès rapide parc <-> entrées

V1.0.16-20250206

    ajout du monitoring de l'application (micrometer, prometheus)
    amélioration de l'algorithme de fusion des isochrones

V1.0.15-20250129

    fix: optimisation, relocalisation automatique des parcs sur la ville

V1.0.14-20250114

    fix: edition de contours de parc de la couche autmel

V1.0.13-20250106

    fix: revue du pool du SGBDR

V1.0.12-20250106

    correctif: les parcs sans contours apparaissent avec un marqueur si les coordonnées existent.
    montée de version des librairies dépendantes

V1.0.11-20241231

    Sélecteur de région, ville et d'adresse sur la carte publique
    Refonte du moteur des couleurs sur la carte publique (optimisation et réduction des tailles)
    Publication des cartes pour l'administateur.
    Intégration du critère 0,5 ha à proximité.
    Corrections: timehack, rafraichissement des parcs dans les formulaires de saisie
    Ajouter les parcs de l'OpenData Lyon, Nantes & Toulouse

V1.0.10-20241205

    Création de parc
    Modification du sélecteur de ville
    Page de login custom
    Homogénéisation des boutons.
    Migration technique de la carte publique de vue2 à vue3
    Démo de statistiques d'accès aux parcs

V1.0.9-20240717

    Carreaux de 200m au national
    Donnée carroyées de 2015, 2017, 2019
    Données de villes et des références de densité
    Intégration des nouvelles API de l'IGN
    Refonte du moteur de calcul et optimisations
    nouveau rendu de la carte publique
    publication de la carte publique pré-calculée (publication depuis l'app de gestion faisable manuellement en attendant))

V1.0.8-20240310

    Info parc, surface, m²/hab, type, status
    Ajouter un typage pour les parcs (parc, bosquet, cimetière).
    Ajouter un status pour les parcs (fait, a faire, a recalculer).

V1.0.7-20240229

    FIX Sécurité CVE-2024-25710, CVE-2024-1597
    Correction des stats sur les isochrones

V1.0.6-20240216

    Rendre disponible depuis l'interface la fusion des isochrones des entrées
    lancer depuis l'ihm la fusion des isochrones des entrées de parc.

V1.0.5-20240205

    Réactivation de l'envoi de mail.
    Possibilité de régénérer un mot de passe.
    Page d'information CNIL et sécurité.
    Affichage des isochrones de chaque entrée sur le formulaire de gestion des entrées

V1.0.4-20240202

    Désactivation de l'envoi de mail.

V1.0.3-20240130

    Ajouter les parcs de l'OpenData Roubaix & Tourcoing
    Automatiser l'envoi de mail en cas de création ou de réinitialisation de mot de passe.

V1.0.2-20240125

    Liste et édition des associations
    Liste et édition des utilisateurs

V1.0.1-20240119

    Edition des entrées
    Liste des parcs

V1.0.0-20231010

    Première version

V0.1.0-20230615

    MVP Lille-Hellemmes-Lomme publique

V0.0.1-20230328

    MVP privé Lille-Hellemmes-Lomme
    Moteur geojson
    Saisie GSheet
