-- File d'attente des rafraîchissements NDVI, traitée par lots par NdviBatchJobService.
-- Clé = (parc, année de référence), comme park_area_computed.
CREATE TABLE IF NOT EXISTS public.ndvi_job (
    id BIGINT NOT NULL,
    annee INTEGER NOT NULL,
    demand TIMESTAMP NOT NULL,
    processed TIMESTAMP,
    status INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (id, annee)
);
CREATE INDEX IF NOT EXISTS idx_ndvi_job_status_demand ON public.ndvi_job (status, demand);
