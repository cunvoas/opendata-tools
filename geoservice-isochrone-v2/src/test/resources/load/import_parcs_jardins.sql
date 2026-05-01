SELECT ll.nom, pj.nom_parc
--identifiant, adresse, aire_jeux, coordonnee, etat_ouverture, hierarchie, nom_parc, quartier, sous_type, surface, type, id_city, source, status, contour, date_debut, date_fin, surface_contour, type_id, oms_custom, date_suppr
    FROM public.parc_jardin pj
    full outer join public.lille_parcs_jardins ll on pj.nom_parc=ll.nom
    where pj.id_city=2878 
    
    
    
    -- Script SQL PostgreSQL + PostGIS pour créer et importer les données des parcs et jardins de Lille

-- Créer l'extension PostGIS si elle n'existe pas déjà
CREATE EXTENSION IF NOT EXISTS postgis;

-- Supprimer la table si elle existe
DROP TABLE IF EXISTS lille_parcs_jardins;

-- Créer la table
CREATE TABLE lille_parcs_jardins (
    id NUMERIC,
    nom varchar(2000),
    quartier varchar(2000),
    type_nev varchar(2000),
    sous_type varchar(2000),
    ouvert_au varchar(2000),
    etat_ouver varchar(2000),
    horaires_o varchar(2000),
    horaires_1 varchar(2000),
    aire_jeux varchar(2000),
    nom_liste varchar(2000),
    adresse varchar(2000),
    surface NUMERIC,
    geom GEOMETRY(MultiPolygon, 4326)
);

-- Commande COPY pour importer les données depuis le fichier CSV
-- Note: Remplacer '/chemin/absolu/vers/le/fichier.csv' par le chemin absolu du fichier CSV
\COPY lille_parcs_jardins FROM '/work/PERSO/github/opendata-tools/geoservice-isochrone/src/test/resources/load/lille_parcs_jardins_lille_hellemmes_lomme_emprise.csv' WITH (FORMAT csv, HEADER true, DELIMITER ',', QUOTE '"', ENCODING 'UTF8');

-- Créer un index spatial sur la colonne géométrique pour améliorer les performances
CREATE INDEX idx_parcs_jardins_geom ON lille_parcs_jardins USING GIST(geom);

-- Créer un index sur le nom pour les recherches
CREATE INDEX idx_parcs_jardins_nom ON lille_parcs_jardins(nom);

-- Afficher le nombre de lignes importées
SELECT COUNT(*) AS nb_lignes_importees FROM lille_parcs_jardins;

-- Afficher quelques statistiques
SELECT 
    COUNT(*) AS total,
    COUNT(DISTINCT quartier) AS nb_quartiers,
    COUNT(DISTINCT type_nev) AS nb_types,
    SUM(surface) AS surface_totale,
    AVG(surface) AS surface_moyenne
FROM lille_parcs_jardins;

-- Afficher les premiers enregistrements
SELECT id, nom, quartier, type_nev, surface 
FROM lille_parcs_jardins 
LIMIT 5;
