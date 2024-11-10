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



---  bar graph OMS + pauvres
 
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
) as menage,
(
    select getColorDensite(cc.is_dense, surface_min)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE cc.annee=2019     AND f.lcog_geo like '%59350%'
    AND surface_park_pcapita BETWEEN surface_min AND surface_max
    group by is_dense
) as couleur

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
) as population,
(
    select getColorDensite(cc.is_dense, surface_min)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE cc.annee=2019     AND f.lcog_geo like '%59350%'
    AND surface_park_pcapita BETWEEN surface_min AND surface_max
    group by is_dense
) as couleur

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
) as population,
(
    select getColorDensite(cc.is_dense, surface_min)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE cc.annee=2019     AND f.lcog_geo like '%59350%'
    AND surface_park_pcapita BETWEEN surface_min AND surface_max
    group by is_dense
) as couleur

FROM range
order by surface_min;



-- bar graph OFFICIEL SEUIL
 
 WITH range AS (

    SELECT 0 AS surface_min, 10 AS surface_max
    UNION
    SELECT 10 AS surface_min, 12 AS surface_max
    UNION
    SELECT 12 AS surface_min, 200 AS surface_max
)
SELECT surface_min, surface_max 
, (
    SELECT sum(pop_all)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE cc.annee=2019     AND f.lcog_geo like '%59350%'
    AND surface_park_pcapita BETWEEN surface_min AND surface_max
) as population,
(
    select getColorDensite(cc.is_dense, surface_min)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE cc.annee=2019     AND f.lcog_geo like '%59350%'
    AND surface_park_pcapita BETWEEN surface_min AND surface_max
    group by is_dense
) as couleur

FROM range
order by surface_min;


--====================================================
-- par ages
--====================================================


 WITH series AS (
    SELECT generate_series(0, 170, 1) AS surface_min
), range AS (
    SELECT surface_min, (surface_min + 1) AS surface_max, '59350' as ref_insee, 2019 as an_insee FROM series 
    /*
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
    */
)
SELECT surface_min, surface_max 
, (
    SELECT coalesce(round(sum(pop_inc), 0), 0)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE 
        cc.annee=r.an_insee AND 
        f.lcog_geo like '%'||ref_insee||'%'  AND
        surface_park_pcapita BETWEEN r.surface_min AND r.surface_max
) as pop_inc
, (
    SELECT coalesce(round(sum(pop_exc), 0), 0)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE 
        cc.annee=r.an_insee AND 
        f.lcog_geo like '%'||ref_insee||'%'  AND
        surface_park_pcapita BETWEEN r.surface_min AND r.surface_max
) as pop_exc
, (
    SELECT coalesce(round(sum(ind), 0), 0)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE 
        cc.annee=r.an_insee AND 
        f.lcog_geo like '%'||ref_insee||'%'  AND
        surface_park_pcapita BETWEEN r.surface_min AND r.surface_max
) as ind
, (
    SELECT coalesce(round(sum(ind_0_3), 0), 0)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE 
        cc.annee=r.an_insee AND 
        f.lcog_geo like '%'||ref_insee||'%'  AND
        surface_park_pcapita BETWEEN r.surface_min AND r.surface_max
) as ind_0_3
, (
    SELECT coalesce(round(sum(ind_4_5), 0), 0)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE 
        cc.annee=r.an_insee AND 
        f.lcog_geo like '%'||ref_insee||'%'  AND
        surface_park_pcapita BETWEEN r.surface_min AND r.surface_max
) as ind_4_5
, (
    SELECT coalesce(round(sum(ind_6_10), 0), 0)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE 
        cc.annee=r.an_insee AND 
        f.lcog_geo like '%'||ref_insee||'%'  AND
        surface_park_pcapita BETWEEN r.surface_min AND r.surface_max
) as ind_6_10
, (
    SELECT coalesce(round(sum(ind_11_17), 0), 0)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE 
        cc.annee=r.an_insee AND 
        f.lcog_geo like '%'||ref_insee||'%'  AND
        surface_park_pcapita BETWEEN r.surface_min AND r.surface_max
) as ind_11_17
, (
    SELECT coalesce(round(sum(ind_18_24), 0), 0)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE 
        cc.annee=r.an_insee AND 
        f.lcog_geo like '%'||ref_insee||'%'  AND
        surface_park_pcapita BETWEEN r.surface_min AND r.surface_max
) as ind_18_24
, (
    SELECT coalesce(round(sum(ind_25_39), 0), 0)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE 
        cc.annee=r.an_insee AND 
        f.lcog_geo like '%'||ref_insee||'%'  AND
        surface_park_pcapita BETWEEN r.surface_min AND r.surface_max
) as ind_25_39
, (
    SELECT coalesce(round(sum(ind_40_54), 0), 0)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE 
        cc.annee=r.an_insee AND 
        f.lcog_geo like '%'||ref_insee||'%'  AND
        surface_park_pcapita BETWEEN r.surface_min AND r.surface_max
) as ind_40_54
, (
    SELECT coalesce(round(sum(ind_55_64), 0), 0)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE 
        cc.annee=r.an_insee AND 
        f.lcog_geo like '%'||ref_insee||'%'  AND
        surface_park_pcapita BETWEEN r.surface_min AND r.surface_max
) as ind_55_64
, (
    SELECT coalesce(round(sum(ind_65_79), 0), 0)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE 
        cc.annee=r.an_insee AND 
        f.lcog_geo like '%'||ref_insee||'%'  AND
        surface_park_pcapita BETWEEN r.surface_min AND r.surface_max
) as ind_65_79
, (
    SELECT coalesce(round(sum(ind_80p), 0), 0)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE 
        cc.annee=r.an_insee AND 
        f.lcog_geo like '%'||ref_insee||'%'  AND
        surface_park_pcapita BETWEEN r.surface_min AND r.surface_max
) as ind_80p

, (
    SELECT coalesce(round(sum(men), 0), 0)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE 
        cc.annee=r.an_insee AND 
        f.lcog_geo like '%'||ref_insee||'%'  AND
        surface_park_pcapita BETWEEN r.surface_min AND r.surface_max
) as men
, (
    SELECT coalesce(round(sum(men_pauv), 0), 0)
    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    WHERE 
        cc.annee=r.an_insee AND 
        f.lcog_geo like '%'||ref_insee||'%'  AND
        surface_park_pcapita BETWEEN r.surface_min AND r.surface_max
) as men_pauv

FROM range r
order by r.surface_min;


-- =========================   PAR Age optimisée



WITH series AS (
    SELECT generate_series(0, 170, 1) AS surface_min
), range AS (
    SELECT surface_min, (surface_min + 1) AS surface_max
    FROM series 
), stats AS (
    SELECT 
        cc.annee, f.lcog_geo, coalesce(round(surface_park_pcapita, 2), 0) as surface_park_pcapita,
        coalesce(round(sum(cc.pop_inc), 0), 0) as pop_inc, 
        coalesce(round(sum(cc.pop_exc), 0), 0) as pop_exc,
        coalesce(round(sum(f.ind), 0), 0) as ind,
        coalesce(round(sum(f.ind_0_3), 0), 0) as ind_0_3,
        coalesce(round(sum(f.ind_4_5), 0), 0) as ind_4_5,
        coalesce(round(sum(f.ind_6_10), 0), 0) as ind_6_10,
        coalesce(round(sum(f.ind_11_17), 0), 0) as ind_11_17,
        coalesce(round(sum(f.ind_18_24), 0), 0) as ind_18_24,
        coalesce(round(sum(f.ind_25_39), 0), 0) as ind_25_39,
        coalesce(round(sum(f.ind_40_54), 0), 0) as ind_40_54,
        coalesce(round(sum(f.ind_55_64), 0), 0) as ind_55_64,
        coalesce(round(sum(f.ind_65_79), 0), 0) as ind_65_79,
        coalesce(round(sum(f.ind_80p), 0), 0) as ind_80p,
        coalesce(round(sum(f.men), 0), 0) as men,
        coalesce(round(sum(f.men_pauv), 0), 0) as men_pauv

    FROM public.carre200_computed_v2 cc 
    INNER JOIN public.filosofi_200m f 
        ON cc.annee=f.annee AND cc.id_inspire=idcar_200m
    group by
        cc.annee,
        f.lcog_geo,
        coalesce(round(surface_park_pcapita, 2), 0)
)

SELECT 
    annee, surface_min, surface_max,
    sum(pop_inc) as pop_inc,
    sum(pop_exc) as pop_exc,
    sum(ind) as ind,
    sum(ind_0_3) as ind_0_3,
    sum(ind_4_5) as ind_4_5,
    sum(ind_6_10) as ind_6_10,
    sum(ind_11_17) as ind_11_17,
    sum(ind_18_24) as ind_18_24,
    sum(ind_25_39) as ind_25_39,
    sum(ind_40_54) as ind_40_54,
    sum(ind_55_64) as ind_55_64,
    sum(ind_65_79) as ind_65_79,
    sum(ind_80p) as ind_80p,
    sum(men) as men,
    sum(men_pauv) as men_pauv

FROM range r, stats
WHERE 
    stats.annee=2019 AND
    lcog_geo like '%59350%'
    AND surface_park_pcapita >= r.surface_min AND surface_park_pcapita < r.surface_max
GROUP BY annee, surface_min, surface_max
order by r.surface_min;






