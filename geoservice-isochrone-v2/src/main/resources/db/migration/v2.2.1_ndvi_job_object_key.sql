-- File NDVI : la clé passe de (parc, année) à un objet NDVI (id, id_inspire, object_type).
-- Un seul job par parc : on garde la ligne la plus récente, avec la dernière date
-- traitée toutes années confondues (règle du un appel par an).
-- Pas de NULL dans la clé : id = -1 si id_inspire renseigné, id_inspire = '' sinon.
-- Clé technique job_id (surrogate), unicité sur (id, id_inspire, object_type).
-- Tolère des colonnes déjà présentes (ddl-auto update) : renseignées avant NOT NULL.

CREATE TEMP TABLE ndvi_job_last AS
SELECT DISTINCT ON (id) id, annee, MAX(processed) OVER (PARTITION BY id) AS last_processed
FROM public.ndvi_job
ORDER BY id, COALESCE(processed, demand) DESC, annee DESC;

DELETE FROM public.ndvi_job j
 WHERE NOT EXISTS (SELECT 1 FROM ndvi_job_last l WHERE l.id = j.id AND l.annee = j.annee);

UPDATE public.ndvi_job j
   SET processed = l.last_processed
  FROM ndvi_job_last l
 WHERE l.id = j.id AND l.annee = j.annee;

DROP TABLE ndvi_job_last;

ALTER TABLE public.ndvi_job DROP CONSTRAINT IF EXISTS ndvi_job_pkey;
ALTER TABLE public.ndvi_job DROP COLUMN IF EXISTS annee;

ALTER TABLE public.ndvi_job
    ADD COLUMN IF NOT EXISTS id_inspire VARCHAR(30),
    ADD COLUMN IF NOT EXISTS object_type VARCHAR(30);

UPDATE public.ndvi_job SET id_inspire = '' WHERE id_inspire IS NULL;
UPDATE public.ndvi_job SET object_type = 'PARK_AREA_COMPUTED' WHERE object_type IS NULL;

ALTER TABLE public.ndvi_job
    ALTER COLUMN id_inspire SET NOT NULL,
    ALTER COLUMN object_type SET NOT NULL;

ALTER TABLE public.ndvi_job ADD COLUMN IF NOT EXISTS job_id BIGSERIAL;
ALTER TABLE public.ndvi_job ADD PRIMARY KEY (job_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_ndvi_job_object
    ON public.ndvi_job (id, id_inspire, object_type);
