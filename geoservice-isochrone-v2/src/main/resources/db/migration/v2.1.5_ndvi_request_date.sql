-- Date de la dernière demande NDVI : limite les appels à un par an et par parc.
ALTER TABLE public.park_area_computed
    ADD COLUMN IF NOT EXISTS ndvi_request_date TIMESTAMP;
