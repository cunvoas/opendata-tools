-- Mesures de vegetation Copernicus, independantes de l'annee de calcul.
-- Une ligne = une periode d'observation (mois/jour) pour un objet de parc.
CREATE TABLE IF NOT EXISTS public.ndvi_result (
    id BIGSERIAL PRIMARY KEY,
    object_type VARCHAR(30) NOT NULL,
    park_area_computed_id BIGINT,
    parc_et_jardin_id BIGINT,
    iris_id VARCHAR(9),
    cadastre_id VARCHAR(5),
    carre200m_id VARCHAR(30),
    requested_at TIMESTAMP NOT NULL,
    period_start TIMESTAMP,
    period_end TIMESTAMP,
    sample_count INTEGER,
    ndvi_mean NUMERIC(6,4),
    ndvi_min NUMERIC(6,4),
    ndvi_max NUMERIC(6,4),
    ndvi_stddev NUMERIC(6,4),
    ndre_mean NUMERIC(6,4),
    ndre_min NUMERIC(6,4),
    ndre_max NUMERIC(6,4),
    ndre_stddev NUMERIC(6,4),
    gndvi_mean NUMERIC(6,4),
    gndvi_min NUMERIC(6,4),
    gndvi_max NUMERIC(6,4),
    gndvi_stddev NUMERIC(6,4)
);

CREATE INDEX IF NOT EXISTS idx_ndvi_result_park_period
    ON public.ndvi_result (object_type, park_area_computed_id, period_end);
CREATE INDEX IF NOT EXISTS idx_ndvi_result_parc
    ON public.ndvi_result (parc_et_jardin_id);


