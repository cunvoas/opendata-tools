-- parcs
SELECT 
    pa.id, pa.name, pa.block  --, pa.type_id, pa.oms_custom
    , pac.oms, pac.population, pac.surface, pac.surface_population, pac.dense, pac.annee
    , pj.aire_jeux, pj.etat_ouverture, pj.nom_parc, pj.quartier, pj.sous_type, pj.surface, pj.type, pj.id_city --, pj.source, pj.status

    FROM public.park_area pa
    INNER JOIN public.park_area_computed pac 
        ON pa.id = pac.id
    INNER JOIN public.parc_jardin pj
        ON pj.identifiant = pa.id_parc_jardin

WHERE 
    pac.annee=2019
    
-- carreaux

SELECT 
pop_all, surface_park_pcapita, surface_park_pcapita_oms

--  cc.annee, is_dense, pop_all, pop_exc, pop_exc_oms, pop_inc, pop_inc_oms,  surface_park_pcapita, surface_park_pcapita_oms, surface_access_park, surface_access_park_oms, updated
--  ,  ind, men_1ind, men_5ind, men_prop, men_fmp, ind_snv, men_surf, men_coll, men_mais, log_av45, log_45_70, log_70_90, log_ap90, log_inc, log_soc, ind_0_3, ind_4_5, ind_6_10, ind_11_17, ind_18_24, ind_25_39, ind_40_54, ind_55_64, ind_65_79, ind_80p, ind_inc, men_pauv, men, lcog_geo

-- cc.parc_comment
--idcar_200m, i_est_200, idcar_1km, i_est_1km, idcar_nat,
FROM public.carre200_computed_v2 cc 
INNER JOIN public.filosofi_200m f 
    ON cc.annee=f.annee AND cc.id_inspire=idcar_200m

WHERE 
cc.annee=2019
AND f.lcog_geo like '%59350%'

order by surface_park_pcapita_oms

--LIMIT 10



---
 bar graph OMS + pauvres
 
 WITH series AS (
    SELECT generate_series(0, 11, 1) AS surface_min
), range AS (
    SELECT surface_min, (surface_min + 1) AS surface_max FROM series 
    UNION
    SELECT 12 AS r_from, 15 AS surface_max
    UNION
    SELECT 15 AS surface_min, 20 AS surface_max
    UNION
    SELECT 20 AS surface_min, 25 AS surface_max
    UNION
    SELECT 25 AS surface_min, 45 AS surface_max
    UNION
    SELECT 45 AS surface_min, 200 AS surface_max
)
SELECT surface_min, surface_max 
, (
    SELECT round(sum(pop_all), 0)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE cc.annee=2019     AND f.lcog_geo like '%59350%'
    AND surface_park_pcapita_oms BETWEEN surface_min AND surface_max
) as population
, (
    SELECT round(sum(men_pauv), 0)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE cc.annee=2019     AND f.lcog_geo like '%59350%'
    AND surface_park_pcapita_oms BETWEEN surface_min AND surface_max
) as menage_pauvre
, (
    SELECT round(sum(men), 0)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE cc.annee=2019     AND f.lcog_geo like '%59350%'
    AND surface_park_pcapita_oms BETWEEN surface_min AND surface_max
) as menage

FROM range
order by surface_min;
 
 
---
 bar graph OMS
 
 WITH series AS (
    SELECT generate_series(0, 11, 1) AS surface_min
), range AS (
    SELECT surface_min, (surface_min + 1) AS surface_max FROM series 
    UNION
    SELECT 12 AS r_from, 15 AS surface_max
    UNION
    SELECT 15 AS surface_min, 20 AS surface_max
    UNION
    SELECT 20 AS surface_min, 25 AS surface_max
    UNION
    SELECT 25 AS surface_min, 45 AS surface_max
    UNION
    SELECT 45 AS surface_min, 200 AS surface_max
)
SELECT surface_min, surface_max 
, (
    SELECT round(sum(pop_all), 0)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE cc.annee=2019     AND f.lcog_geo like '%59350%'
    AND surface_park_pcapita_oms BETWEEN surface_min AND surface_max
) as population

FROM range
order by surface_min;



-- bar graph OFFICIEL
 
 WITH series AS (
    SELECT generate_series(0, 11, 1) AS surface_min
), range AS (
    SELECT surface_min, (surface_min + 1) AS surface_max FROM series 
    UNION
    SELECT 12 AS r_from, 15 AS surface_max
    UNION
    SELECT 15 AS surface_min, 20 AS surface_max
    UNION
    SELECT 20 AS surface_min, 25 AS surface_max
    UNION
    SELECT 25 AS surface_min, 45 AS surface_max
    UNION
    SELECT 45 AS surface_min, 200 AS surface_max
)
SELECT surface_min, surface_max 
, (
    SELECT sum(pop_all)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE cc.annee=2019     AND f.lcog_geo like '%59350%'
    AND surface_park_pcapita BETWEEN surface_min AND surface_max
) as population

FROM range
order by surface_min;
    