-- Historique des observations NDVI retournées par Copernicus.
-- Une ligne par réponse contenant des statistiques, pour pouvoir reventiler
-- les valeurs sur les bonnes années et reconstituer l'histoire végétale du parc.
CREATE TABLE IF NOT EXISTS public.ndvi_observation (
    id BIGSERIAL PRIMARY KEY,
    park_area_id BIGINT NOT NULL,
    requested_at TIMESTAMP NOT NULL,
    period_start TIMESTAMP,
    period_end TIMESTAMP,
    sample_count INTEGER,
    ndvi_mean NUMERIC(6,4),
    ndvi_min NUMERIC(6,4),
    ndvi_max NUMERIC(6,4),
    ndvi_stddev NUMERIC(6,4)
);
CREATE INDEX IF NOT EXISTS idx_ndvi_observation_park_period
    ON public.ndvi_observation (park_area_id, period_start);
