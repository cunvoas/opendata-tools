-- SAVI (B08/B04 avec correction du sol L = 0,5) : quatrième indice Sentinel-2 calculé
-- dans le même appel CDSE que NDVI, NDRE et GNDVI.
ALTER TABLE public.ndvi_result
    ADD COLUMN IF NOT EXISTS savi_mean NUMERIC(6,4),
    ADD COLUMN IF NOT EXISTS savi_min NUMERIC(6,4),
    ADD COLUMN IF NOT EXISTS savi_max NUMERIC(6,4),
    ADD COLUMN IF NOT EXISTS savi_stddev NUMERIC(6,4);

ALTER TABLE public.park_area_computed
    ADD COLUMN IF NOT EXISTS savi_mean NUMERIC(6,4),
    ADD COLUMN IF NOT EXISTS savi_min NUMERIC(6,4),
    ADD COLUMN IF NOT EXISTS savi_max NUMERIC(6,4),
    ADD COLUMN IF NOT EXISTS savi_stddev NUMERIC(6,4);

ALTER TABLE public.ndvi_observation
    ADD COLUMN IF NOT EXISTS savi_mean NUMERIC(6,4),
    ADD COLUMN IF NOT EXISTS savi_min NUMERIC(6,4),
    ADD COLUMN IF NOT EXISTS savi_max NUMERIC(6,4),
    ADD COLUMN IF NOT EXISTS savi_stddev NUMERIC(6,4);
