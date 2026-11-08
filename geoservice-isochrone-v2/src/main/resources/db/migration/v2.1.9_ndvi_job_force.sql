-- Relance NDVI forcée : permet à un administrateur de court-circuiter
-- la règle « un appel Copernicus par parc et par an ».
ALTER TABLE public.ndvi_job
    ADD COLUMN IF NOT EXISTS force BOOLEAN NOT NULL DEFAULT FALSE;
