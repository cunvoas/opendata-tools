-- NDVI par parc : statistiques NDVI stockées sur chaque ligne park_area_computed.
ALTER TABLE public.park_area_computed
    ADD COLUMN IF NOT EXISTS ndvi_mean NUMERIC(6,4),
    ADD COLUMN IF NOT EXISTS ndvi_min NUMERIC(6,4),
    ADD COLUMN IF NOT EXISTS ndvi_max NUMERIC(6,4),
    ADD COLUMN IF NOT EXISTS ndvi_stddev NUMERIC(6,4),
    ADD COLUMN IF NOT EXISTS ndvi_acquisition_date TIMESTAMP;
