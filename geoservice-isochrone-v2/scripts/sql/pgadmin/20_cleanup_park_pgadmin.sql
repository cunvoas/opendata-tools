-- Nettoyage d'un doublon de parc — version pgAdmin (pas de variables psql).
--
-- MODE PAR DÉFAUT : ROLLBACK (aucune suppression). Vérifier les compteurs affichés,
-- puis remplacer ROLLBACK par COMMIT en fin de script et ré-exécuter.
--
-- ÉTAPE 1 : éditer UNIQUEMENT le bloc "CIBLE" ci-dessous.
--           Mettre l'id dans park_area_id OU dans parc_jardin_id, l'autre à NULL.
-- ÉTAPE 2 : exécuter tout le script (F5) dans le Query Tool.
-- ÉTAPE 3 : lire les compteurs DELETE, puis COMMIT si tout est correct.
--
-- Sauvegarde préalable recommandée :
--   pg_dump "$DB_URL" -t park_area -t park_entrance -t park_area_computed \
--     -t ndvi_job -t ndvi_observation -t ndvi_result -t parc_prefecture -t parc_photo \
--     > /tmp/cleanup_backup_$(date +%Y%m%d_%H%M%S).sql
-- ou clic droit sur la base > Backup dans pgAdmin.

BEGIN;

-- ===== CIBLE : éditer ces 2 valeurs =====
CREATE TEMP TABLE tmp_cible ON COMMIT DROP AS
SELECT 346::bigint AS park_area_id,   -- <- id park_area à supprimer, sinon NULL
       NULL::bigint AS parc_jardin_id -- <- id parc_jardin à supprimer, sinon NULL
;
-- Variante parc_jardin (exemple) :
-- CREATE TEMP TABLE tmp_cible ON COMMIT DROP AS
-- SELECT NULL::bigint AS park_area_id, 129::bigint AS parc_jardin_id;

-- Bloque les écritures concurrentes (batch NDVI / calcul) pendant le nettoyage.
LOCK TABLE public.park_area, public.park_area_computed, public.ndvi_job,
           public.ndvi_observation, public.ndvi_result, public.park_entrance,
           public.parc_prefecture, public.parc_photo
      IN SHARE ROW EXCLUSIVE MODE;

-- Vérification avant suppression.
SELECT c.park_area_id, c.parc_jardin_id,
       (SELECT array_agg(id ORDER BY id) FROM public.park_area a
         WHERE a.id = c.park_area_id OR a.id_parc_jardin = c.parc_jardin_id) AS park_area_ids
FROM tmp_cible c;

-- Restructuration possible ? Parcs homonymes sur d'autres périodes (date_debut
-- absente = 1900-01-01, date_fin absente = 2199-12-31).
-- periodes_disjointes = true -> les deux parcs sont légitimes : NE PAS SUPPRIMER.
WITH cible AS (
  SELECT regexp_replace(lower(trim(coalesce(a.name, pj.nom_parc))), '\s+', ' ', 'g') AS nom_key,
         coalesce(pj.date_debut, '1900-01-01') AS d_debut,
         coalesce(pj.date_fin,   '2199-12-31') AS d_fin
  FROM tmp_cible c
  LEFT JOIN public.park_area a    ON a.id = c.park_area_id
  LEFT JOIN public.parc_jardin pj ON pj.identifiant = coalesce(a.id_parc_jardin, c.parc_jardin_id)
),
h AS (
  SELECT pj.identifiant, pj.nom_parc, pj.date_debut, pj.date_fin,
         regexp_replace(lower(trim(pj.nom_parc)), '\s+', ' ', 'g') AS nom_key
  FROM public.parc_jardin pj
  WHERE pj.identifiant IS DISTINCT FROM (SELECT parc_jardin_id FROM tmp_cible)
)
SELECT h.identifiant, h.nom_parc, h.date_debut, h.date_fin,
       (coalesce(h.date_fin, '2199-12-31') < c.d_debut
        OR c.d_fin < coalesce(h.date_debut, '1900-01-01')) AS periodes_disjointes
FROM h, cible c
WHERE h.nom_key = c.nom_key
   OR (length(h.nom_key) > 3 AND c.nom_key LIKE h.nom_key || '%')
   OR (length(c.nom_key) > 3 AND h.nom_key LIKE c.nom_key || '%')
ORDER BY h.identifiant ASC;

-- Cible effective : un park_area précis, ou tous ceux du parc_jardin.
CREATE TEMP TABLE tmp_del_area ON COMMIT DROP AS
SELECT a.id
FROM public.park_area a, tmp_cible c
WHERE a.id = c.park_area_id
   OR a.id_parc_jardin = c.parc_jardin_id;

-- Enfants d'abord.
DELETE FROM public.ndvi_result
 WHERE park_area_computed_id IN (SELECT id FROM tmp_del_area)
    OR parc_et_jardin_id = (SELECT parc_jardin_id FROM tmp_cible);

DELETE FROM public.ndvi_observation
 WHERE park_area_id IN (SELECT id FROM tmp_del_area);

DELETE FROM public.ndvi_job
 WHERE id IN (SELECT id FROM tmp_del_area);

DELETE FROM public.park_area_computed
 WHERE id IN (SELECT id FROM tmp_del_area);

DELETE FROM public.park_entrance
 WHERE area_id IN (SELECT id FROM tmp_del_area);

DELETE FROM public.park_area
 WHERE id IN (SELECT id FROM tmp_del_area);

DELETE FROM public.parc_prefecture
 WHERE id_parc = (SELECT parc_jardin_id FROM tmp_cible);

DELETE FROM public.parc_photo
 WHERE parc_id = (SELECT parc_jardin_id FROM tmp_cible);

DELETE FROM public.parc_jardin
 WHERE identifiant = (SELECT parc_jardin_id FROM tmp_cible);

-- ===== Fin : ROLLBACK par sécurité =====
-- Remplacer ROLLBACK par COMMIT après vérification des lignes ci-dessus.
ROLLBACK;
