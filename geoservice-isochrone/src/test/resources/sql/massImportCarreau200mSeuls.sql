--####################################################
-- Code Java inefficace (MassGeoJsonIntegratorParser)
--   gain : 528x
--####################################################

-- décommenter pour vider la table
--truncate public.carre200onlyshape;



truncate public.carre200_json;

-- IMPORT en masse (13,7M Lignes) du fichier GeoJson (1 ligne = 1 carreau)  : 38s
-- avec psql, intégration binaire
copy public.carre200_json (data) FROM '/work/PERSO/ASSO/data/grilleFull_gps/grille200m-geojson.json' ENCODING 'UTF8';



-- convertion dans la table définitive (10 min)
--   les fonctions REPLACE manipulent le GeoJson au format SQL
INSERT INTO public.carre200onlyshape( id_inspire, id_carre_1km, geo_shape )
SELECT 
    data::json->'properties' ->> 'idINSPIRE' as id_inspire, 
    data::json->'properties' ->> 'id_carr_1k' as id_carr_1k, 
    ST_GeomFromText(
        REPLACE(
            REPLACE(
                REPLACE(
                    REPLACE(
                        data::json->'geometry' ->>'coordinates', 
                        '[ [ [ [', 'POLYGON((' ), 
                    '] ] ] ]', '))' ),
             ', ', ' ' )
        , ' ] [ ',' , ')
    , 4326)  as poly
    FROM public.carre200_json;
    
-- calcul des centres de carreaux (28 min)
 UPDATE public.carre200onlyshape SET geo_point_2d = ST_Centroid(geo_shape);
    
-- ajout de l'ID INSEE (20 min)
 UPDATE public.carre200onlyshape 
  SET code_insee = c.id_insee
    FROM public.cadastre c 
    WHERE ST_INTERSECTS(c.geo_shape,  geo_point_2d);

 -- restructuration de la table (4 min)
 VACUUM (FULL, ANALYZE) public.carre200onlyshape;
  -- reindexation (3 min)
 REINDEX TABLE public.carre200onlyshape;
 
 
 --   update pg_index set indisvalid = false where indexrelid = 'test_pkey'::regclass;
    
-- tag les carreaux avec population (12,5 min)  2287214 UPDATED
UPDATE public.carre200onlyshape  cs200
    SET avec_pop = true
    FROM  public.filosofi_200m f    
    WHERE cs200.id_inspire = f.idcar_200m and f.annee=2019 and cs200.avec_pop is null;  
    
-- tag les carreaux avec population suite (1,5 min)  139660 UPDATED
UPDATE public.carre200onlyshape  cs200 
    SET avec_pop = true
    FROM  public.filosofi_200m f    
    WHERE cs200.id_inspire = f.idcar_200m and f.annee in (2017, 2015) and cs200.avec_pop is null;  

-- tag les carreaux sans population (10 min)  11333945 UPDATED
  UPDATE public.carre200onlyshape SET avec_pop = false WHERE avec_pop is null;
1h27
  
 -- restructuration de la table (4 min)
 VACUUM (FULL, ANALYZE) public.carre200onlyshape;
  -- reindexation (3 min)
 REINDEX TABLE public.carre200onlyshape;
 