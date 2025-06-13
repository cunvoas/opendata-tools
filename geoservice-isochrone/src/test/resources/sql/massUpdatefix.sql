update public.parc_jardin 
 	set surface_contour= ROUND(ST_Area(contour, true), 1)
	where surface_contour is null and contour is not null;
	
update public.parc_jardin 
 	set coordonnee= ST_Centroid(contour, true)
	where coordonnee is null and contour is not null;	
	
update public.parc_jardin 
 	set coordonnee= ST_PointFromText( ST_asText( ST_Centroid(contour, true)))
	where coordonnee is null and contour is not null;	
	
UPDATE public.parc_jardin 
	SET  type_id=pa.type_id
FROM ( SELECT 
 	id_parc_jardin, type_id, oms_custom
	FROM public.park_area ) pa	
WHERE identifiant=pa.id_parc_jardin ;

UPDATE public.parc_jardin 
SET type_id=pa.type_id, oms_custom=pa.oms_custom 
FROM   public.park_area pa 
WHERE  pa.id_parc_jardin = identifiant and public.parc_jardin.oms_custom is null ;

	
UPDATE public.parc_jardin SET type_id=1;
UPDATE public.parc_jardin SET type_id=4 WHERE sous_type='Jardin de poche';
UPDATE public.parc_jardin SET type_id=5 WHERE sous_type='Place';
UPDATE public.parc_jardin SET type_id=6 WHERE type='Cimetière';



--== SEQUENCES ==--

ALTER SEQUENCE IF EXISTS public.seq_activity_stats INCREMENT 1 START 1;
ALTER SEQUENCE IF EXISTS public.seq_asso INCREMENT 1 START 1;
ALTER SEQUENCE IF EXISTS public.seq_city INCREMENT 1 START 1;
ALTER SEQUENCE IF EXISTS public.seq_com2com INCREMENT 1 START 1;
ALTER SEQUENCE IF EXISTS public.seq_contrib INCREMENT 1 START 1;
ALTER SEQUENCE IF EXISTS public.seq_dashboard INCREMENT 1 START 1;
ALTER SEQUENCE IF EXISTS public.seq_mel_park INCREMENT 1 START 1;
ALTER SEQUENCE IF EXISTS public.seq_park INCREMENT 1 START 1;
ALTER SEQUENCE IF EXISTS public.seq_park_entrance INCREMENT 1 START 1;
ALTER SEQUENCE IF EXISTS public.seq_park_pref INCREMENT 1 START 1;
ALTER SEQUENCE IF EXISTS public.seq_region INCREMENT 1 START 1;

SELECT setval('public.seq_activity_stats', (select max(id)+1 from public.adm_activity_stats), true);
SELECT setval('public.seq_asso', (select max(id)+1 from public.adm_asso), true);
SELECT setval('public.seq_city', (select max(id)+1 from public.city), true);
SELECT setval('public.seq_com2com', (select max(id)+1 from public.adm_com2commune), true);
SELECT setval('public.seq_contrib', (select max(id)+1 from public.adm_contrib), true);
SELECT setval('public.seq_dashboard', (select max(id)+1 from public.dashboard), true);
SELECT setval('public.seq_mel_park', (select max(identifiant)+1 from public.parc_jardin), true);
SELECT setval('public.seq_park', (select max(id)+1 from public.park_area), true);
SELECT setval('public.seq_park_entrance', (select max(id)+1 from public.park_entrance), true);
SELECT setval('public.seq_park_pref', (select max(identifiant)+1 from public.parc_prefecture), true);
SELECT setval('public.seq_region', (select max(id)+1 from public.adm_region), true);


UPDATE public.iris_shape
    SET
        coordonnee= ST_PointFromText( ST_asText( ST_Centroid(contour, true))) ,
        surface = ST_Area(contour, true)
    WHERE coordonnee is null or surface is null;
 


