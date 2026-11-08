
-- truncate public.ndvi_result;



-- Reprise limitee a 2021 : les mesures NDVI historiques sont portees par
-- park_area_computed pour cette annee.
-- Les identifiants IRIS et carreau 200m ne sont pas recharges pour l'historique.
INSERT INTO public.ndvi_result (
    object_type, park_area_computed_id,
    parc_et_jardin_id, iris_id, cadastre_id, carre200m_id,
    requested_at, period_start, period_end, sample_count,
    ndvi_mean, ndvi_min, ndvi_max, ndvi_stddev,
    ndre_mean, ndre_min, ndre_max, ndre_stddev,
    gndvi_mean, gndvi_min, gndvi_max, gndvi_stddev
)
SELECT
    'PARK_AREA_COMPUTED',
    pac.id,
    pa.id_parc_jardin,
    NULL,
    ci.insee_code,
    NULL,
    COALESCE(pac.ndvi_request_date, pac.updated, pac.ndvi_acquisition_date, now()),
    NULL,
    pac.ndvi_acquisition_date,
    NULL,
    pac.ndvi_mean, pac.ndvi_min, pac.ndvi_max, pac.ndvi_stddev,
    pac.ndre_mean, pac.ndre_min, pac.ndre_max, pac.ndre_stddev,
    pac.gndvi_mean, pac.gndvi_min, pac.gndvi_max, pac.gndvi_stddev
FROM public.park_area_computed pac
JOIN public.park_area pa ON pa.id = pac.id
LEFT JOIN public.parc_jardin pj ON pj.identifiant = pa.id_parc_jardin
LEFT JOIN public.city ci ON ci.id = pj.id_city
WHERE pac.annee = 2021
  AND pac.ndvi_mean IS NOT NULL;
  
-- A JOUER MANUELLEMENT, apres deploiement du code qui lit ndvi_result.
-- Les mesures ont ete recopiees par la migration v2.2.0_ndvi_result.sql.
ALTER TABLE public.park_area_computed
    DROP COLUMN IF EXISTS ndvi_mean,
    DROP COLUMN IF EXISTS ndvi_min,
    DROP COLUMN IF EXISTS ndvi_max,
    DROP COLUMN IF EXISTS ndvi_stddev,
    DROP COLUMN IF EXISTS ndre_mean,
    DROP COLUMN IF EXISTS ndre_min,
    DROP COLUMN IF EXISTS ndre_max,
    DROP COLUMN IF EXISTS ndre_stddev,
    DROP COLUMN IF EXISTS gndvi_mean,
    DROP COLUMN IF EXISTS gndvi_min,
    DROP COLUMN IF EXISTS gndvi_max,
    DROP COLUMN IF EXISTS gndvi_stddev,
    DROP COLUMN IF EXISTS ndvi_acquisition_date,
    DROP COLUMN IF EXISTS ndvi_request_date;

-- Optionnel : ancien historique, a supprimer seulement apres verification des donnees.
-- DROP TABLE IF EXISTS public.ndvi_observation;
ALTER TABLE public.ndvi_result
    DROP COLUMN IF EXISTS park_area_computed_annee;