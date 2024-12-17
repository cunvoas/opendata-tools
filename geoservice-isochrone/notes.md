# notes en vrac

Inspiré de 
   https://houarinourreddine.medium.com/integrate-spring-boot-and-postgis-to-manage-spatial-data-272edacf2cb
   
Bannière en "DOS Rebel"
   https://springhow.com/spring-boot-banner-generator/
   
   
Requete recoupement des Carre avec le Polygon isochrone de parc

SELECT ca.id, ca.ind_c, cs.code, cs.commune, cs.geo_shape, pa.polygon
 FROM park_area pa, carre200shape cs inner join public.carre200 ca on cs.id_carre_hab=ca.id
 where ST_Intersects(pa.polygon, cs.geo_shape) and ST_Intersects(pa.polygon,'SRID=4326;POLYGON((3.10903 50.62347,3.1090273 50.6240298,3.1088697 50.6249568,3.1088697 50.6249568,3.10903 50.62347))')
	 
	 
	 
https://docs.postgresql.fr/11/indexes-types.html
	 
CREATE INDEX idx_parkarea_polygon
    ON public.park_area USING spgist
    (polygon)
    TABLESPACE pg_default;
CREATE INDEX idx_parkarea_point
    ON public.park_area USING spgist
    (point)
    TABLESPACE pg_default;
    
CREATE INDEX idx_carre200_shape
    ON public.carre200shape USING spgist
    (geo_shape)
    TABLESPACE pg_default;

drop index idx_cadastre_shape;
CREATE INDEX idx_cadastre_shape
    ON public.cadastre USING spgist
    (geo_shape)
    TABLESPACE pg_default;
    
    
DROP TABLE public.laposte;
CREATE TABLE public.laposte (
	code_commune_INSEE character varying(5) NOT NULL,
	nom_commune character varying(50) NOT NULL,
	code_postal character varying(5) NOT NULL,
	Ligne_5 character varying(50),
	Libelle_acheminement character varying(50) NOT NULL,
	coordonnees_gps character varying(50)
);

ALTER TABLE public.laposte OWNER TO insee;
COPY public.laposte FROM '/home/cus/Téléchargements/laposte_hexasmal.csv' delimiter ';' csv header; 


	--coordonnees_gps public.geometry(Point,4326)
	
-- requete ville
select ci.*, ST_Area(ca.geo_shape, true)
from city ci 
	inner join cadastre ca on ci.insee_code=ca.id_insee
	inner join laposte lp on ci.insee_code=lp.code_commune_insee
where ca.nom like 'LIL%' and ca.id_insee like '59%'


round(ST_Area(geo_shape, true)::numeric, 0),

-- requete carre+shape+population
SELECT 
	round(ST_Area(cs.geo_shape, true)::numeric, 0),
	cs.*, ca.id, ca.ind_c
FROM carre200shape cs inner join carre200 ca on ca."idINSPIRE"=cs.id_inspire
WHERE cs.code='59350'





--===========  REQUETE ANALYSE POPULATION VILLE

WITH 
 cadastre as (
	 SELECT c.id_insee, c.nom, l.coordonnees_gps, c.geo_shape
	 FROM cadastre c INNER JOIN laposte l on c.id_insee=l.code_commune_insee
  ),
  population as (
	  SELECT c.ind_c, LPAD(s.code, 5,'0') as id_insee, s.geo_shape, s.id_carre_hab, s.id_inspire
	  FROM carre200shape s INNER JOIN carre200 c ON s.id_inspire=c."idINSPIRE"
  ),
  parc as (
  	SELECT p.name, p.point, p.polygon FROM park_area p
  )

SELECT DISTINCT pop.ind_c, pop.id_insee, pop.geo_shape, pop.id_inspire, parc.name
FROM 
	cadastre c, 
	population pop,
	parc 

WHERE UPPER(c.nom)='LILLE' 
-- restriction au carroyage de la ville par zone géographique
AND ST_Intersects(c.geo_shape, pop.geo_shape)
-- restriction au carroyage des parcs par zone géographique
AND ST_Intersects(pop.geo_shape, parc.polygon)



rien à mois de 333m
respect des 10m2 par habitant > dégradé vers si OK rouge si KO
en milieur urbain

en péri-urbain
25 m2/hab 
isochrone ?



idées pour la suite ::
ZONE SEVESO
les PEFC



  
  
WITH 
  population as (
	SELECT c.ind_c, s.geo_shape, s.id_carre_hab, s.id_inspire
	FROM	carre200shape s 
		INNER JOIN carre200 c 
		ON s.id_inspire=c.id_inspire
  ),
  parc as (
  	SELECT p.polygon FROM park_area p
  )
  
SELECT *
FROM population pop
WHERE pop.id_inspire='LAEA200M_N15400E19168'


===========
SELECT DISTINCT pop.ind_c, pop.id_insee, pop.geo_shape, pop.id_inspire, parc.name
FROM 
	cadastre c, 
	population pop,
	parc 

WHERE UPPER(c.nom)='LILLE' 
-- restriction au carroyage de la ville par zone géographique
AND ST_Intersects(c.geo_shape, pop.geo_shape)
-- restriction au carroyage des parcs par zone géographique
AND ST_Intersects(pop.geo_shape, parc.polygon)

  
LAEA200M_N15400E19168



WITH 
  population as (
	SELECT c.ind_c, s.geo_shape, s.id_carre_hab, s.id_inspire
	FROM	carre200shape s 
		INNER JOIN carre200 c 
		ON s.id_inspire=c.id_inspire
  ),
  parc as (
  	SELECT p.polygon FROM park_area p
  )

SELECT pop.id_carre_hab, pop.id_inspire, pop.ind_c, parc.polygon, pop.geo_shape
FROM population pop, parc
WHERE pop.id_carre_hab = 'LAEA200M_N15400E19168'
AND ST_intersects(parc.polygon, pop.geo_shape)
ORDER BY pop.id_carre_hab


SELECT s.id_carre_hab, s.id_inspire, c.ind_c, s.geo_shape, p.polygon
FROM park_area p, carre200shape s INNER JOIN carre200 c ON s.id_inspire=c.id_inspire
WHERE s.id_carre_hab = 'LAEA200M_N15400E19168' AND ST_intersects(p.polygon, s.geo_shape)
ORDER BY s.id_carre_habg



https://www.peko-step.com/en/tool/colorchart_en.html

https://www.montreuil.fr/fileadmin/user_upload/12_Environnement/06_Etat_des_lieux_de_l_environnement/01_L_observatoire_de_l_environnement/fiche25.pdf



--=====    ANALYSE par VILLE
---==== LISTE 
WITH 
 carre AS (
	SELECT cc.*, cs.geo_shape
	FROM 
		carre200shape cs  INNER JOIN carre200 c 
			ON cs.id_inspire=c.id_inspire
		INNER JOIN carre200_computed_v2 cc ON c.id=cc.id
	),
  ville AS (
  	SELECT c.nom, c.id_insee, c.geo_shape FROM cadastre c
  )
SELECT v.id_insee, v.nom, c.id, c.pop_all, c.pop_inc, c.pop_exc
FROM carre c, ville v
WHERE ST_intersects(c.geo_shape, v.geo_shape)
AND v.nom='LILLE'

--======== SOMME et RATIO

WITH 
 carre AS (
	SELECT cc.*, cs.geo_shape
	FROM 
		carre200shape cs  
		INNER JOIN carre200 c ON cs.id_inspire=c.id_inspire
		INNER JOIN carre200_computed_v2 cc ON c.id=cc.id
	),
  ville AS (
  	SELECT c.nom, c.id_insee, c.geo_shape FROM cadastre c
  )
SELECT v.id_insee, v.nom, 
--c.id, 
sum(c.pop_all), sum(c.pop_inc), sum(c.pop_exc), sum(c.pop_exc)/sum(c.pop_all)
FROM carre c, ville v
WHERE ST_intersects(c.geo_shape, v.geo_shape)
AND v.nom='LILLE'

GROUP BY v.id_insee, v.nom

=================
Population ville
WITH 
 carre AS (
    SELECT cs.geo_shape, c.*
    FROM 
        carre200shape cs  
        INNER JOIN carre200 c 
            ON cs.id_inspire=c.id_inspire
    ),
  ville AS (
    SELECT c.nom, c.id_insee, c.geo_shape FROM cadastre c
  )
SELECT v.id_insee, v.nom, sum(CAST(c.ind_c AS DECIMAL))
FROM carre c, ville v
WHERE ST_intersects(c.geo_shape, v.geo_shape)
AND v.nom='LILLE'

group by v.id_insee, v.nom






============


#  JWT
https://www.bezkoder.com/spring-boot-vue-js-authentication-jwt-spring-security/
https://www.bezkoder.com/vue-3-authentication-jwt/

# Icons
https://fontawesome.com/search?q=map&o=r&m=free

# BootStrap
https://www.w3schools.com/bootstrap4/bootstrap_grid_system.asp