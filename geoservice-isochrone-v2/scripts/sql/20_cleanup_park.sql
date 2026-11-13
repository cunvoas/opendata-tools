-- Nettoyage d'un doublon de parc (park_area ou parc_jardin).
-- ⚠️ Écriture : rien n'est supprimé sans -v confirm=DELETE.
-- Toutes les suppressions des différentes tables sont dans UNE SEULE transaction
-- (BEGIN ... COMMIT) : tout ou rien. Une erreur annule l'ensemble.
--
-- Usage (analyse, aucune écriture) :
--   psql "$DB_URL" -f scripts/sql/20_cleanup_park.sql -v park_area_id=346
--   psql "$DB_URL" -f scripts/sql/20_cleanup_park.sql -v parc_jardin_id=129
--
-- Usage (suppression réelle, transaction unique) :
--   pg_dump "$DB_URL" -t park_area -t park_entrance -t park_area_computed \
--            -t ndvi_job -t ndvi_observation -t ndvi_result -t parc_prefecture -t parc_photo \
--            > /tmp/cleanup_backup_$(date +%Y%m%d_%H%M%S).sql
--   psql "$DB_URL" -f scripts/sql/20_cleanup_park.sql -v park_area_id=346 -v confirm=DELETE
--
-- Un seul des deux paramètres park_area_id / parc_jardin_id doit être fourni.
-- parc_jardin_id supprime AUSSI tous ses park_area et leurs dépendances.
\set ON_ERROR_STOP on
\pset pager off

\if :{?park_area_id}
  \if :{?parc_jardin_id}
    \echo 'ERREUR : fournir park_area_id OU parc_jardin_id, pas les deux.'
    \quit 1
  \endif
\elif :{?parc_jardin_id}
  -- ok
\else
  \echo 'ERREUR : fournir -v park_area_id=... ou -v parc_jardin_id=...'
  \quit 1
\endif

-- Variables de sélection pour la requête 0b : NULL si le paramètre est absent.
-- (Ne pas réutiliser park_area_id / parc_jardin_id : leur existence pilote les \if.)
\if :{?park_area_id}
  \set pa_sel :park_area_id
\else
  \set pa_sel NULL
\endif
\if :{?parc_jardin_id}
  \set pj_sel :parc_jardin_id
\else
  \set pj_sel NULL
\endif

\echo '=== 0b. Homonymes et périodes (restructuration possible ?) ==='
-- Périodes normalisées : date_debut absente = 1900-01-01, date_fin absente =
-- 2199-12-31. periodes_disjointes = true : les deux parcs ne se sont pas
-- chevauchés dans le temps, ils sont légitimes tous les deux : NE PAS SUPPRIMER.
WITH cible AS (
  SELECT a.name AS nom,
         regexp_replace(lower(trim(a.name)), '\s+', ' ', 'g') AS nom_key,
         coalesce(pj.date_debut, '1900-01-01') AS d_debut,
         coalesce(pj.date_fin,   '2199-12-31') AS d_fin
  FROM public.park_area a
  LEFT JOIN public.parc_jardin pj ON pj.identifiant = a.id_parc_jardin
  WHERE a.id = :pa_sel
  UNION ALL
  SELECT pj.nom_parc,
         regexp_replace(lower(trim(pj.nom_parc)), '\s+', ' ', 'g'),
         coalesce(pj.date_debut, '1900-01-01'),
         coalesce(pj.date_fin,   '2199-12-31')
  FROM public.parc_jardin pj
  WHERE pj.identifiant = :pj_sel
),
homonymes AS (
  SELECT pj.identifiant, pj.nom_parc, pj.date_debut, pj.date_fin,
         regexp_replace(lower(trim(pj.nom_parc)), '\s+', ' ', 'g') AS nom_key,
         coalesce(pj.date_debut, '1900-01-01') AS d_debut,
         coalesce(pj.date_fin,   '2199-12-31') AS d_fin,
         c.name AS city, cc.name AS epci
  FROM public.parc_jardin pj
  LEFT JOIN public.city c              ON c.id = pj.id_city
  LEFT JOIN public.adm_com2commune cc  ON cc.id = c.id_comm2co
  WHERE pj.identifiant IS DISTINCT FROM :pj_sel
)
-- Nom identique, ou l'un préfixe de l'autre ("Parc X" / "Parc X (avec ...)"),
-- ce qui couvre les renommages lors d'une restructuration.
SELECT h.identifiant, h.nom_parc, h.date_debut, h.date_fin, h.city, h.epci,
       (h.d_fin < c.d_debut OR c.d_fin < h.d_debut) AS periodes_disjointes
FROM homonymes h
JOIN cible c
  ON h.nom_key = c.nom_key
  OR (length(h.nom_key) > 3 AND c.nom_key LIKE h.nom_key || '%')
  OR (length(c.nom_key) > 3 AND h.nom_key LIKE c.nom_key || '%')
ORDER BY h.identifiant;

\echo '=== 0. Contexte ==='
\if :{?park_area_id}
SELECT a.id, a.name, a.id_parc_jardin,
       pj.nom_parc, c.name AS city, cc.name AS epci, a.updated
FROM public.park_area a
LEFT JOIN public.parc_jardin pj      ON pj.identifiant = a.id_parc_jardin
LEFT JOIN public.city c              ON c.id = pj.id_city
LEFT JOIN public.adm_com2commune cc  ON cc.id = c.id_comm2co
WHERE a.id = :park_area_id;

SELECT count(*) FILTER (WHERE id = :park_area_id)                            AS park_area,
       (SELECT count(*) FROM public.park_entrance WHERE area_id = :park_area_id)         AS park_entrance,
       (SELECT count(*) FROM public.park_area_computed WHERE id = :park_area_id)         AS park_area_computed,
       (SELECT count(*) FROM public.ndvi_job WHERE id = :park_area_id)                   AS ndvi_job,
       (SELECT count(*) FROM public.ndvi_observation WHERE park_area_id = :park_area_id) AS ndvi_observation,
       (SELECT count(*) FROM public.ndvi_result WHERE park_area_computed_id = :park_area_id) AS ndvi_result
FROM public.park_area
WHERE id = :park_area_id;
\elif :{?parc_jardin_id}
SELECT pj.identifiant, pj.nom_parc, c.name AS city, cc.name AS epci, pj.date_suppr
FROM public.parc_jardin pj
LEFT JOIN public.city c              ON c.id = pj.id_city
LEFT JOIN public.adm_com2commune cc  ON cc.id = c.id_comm2co
WHERE pj.identifiant = :parc_jardin_id;

SELECT array_agg(id ORDER BY id) AS park_area_ids
FROM public.park_area WHERE id_parc_jardin = :parc_jardin_id;

SELECT (SELECT count(*) FROM public.park_area WHERE id_parc_jardin = :parc_jardin_id)     AS park_area,
       (SELECT count(*) FROM public.park_entrance
         WHERE area_id IN (SELECT id FROM public.park_area WHERE id_parc_jardin = :parc_jardin_id)) AS park_entrance,
       (SELECT count(*) FROM public.park_area_computed
         WHERE id IN (SELECT id FROM public.park_area WHERE id_parc_jardin = :parc_jardin_id))     AS park_area_computed,
       (SELECT count(*) FROM public.ndvi_job
         WHERE id IN (SELECT id FROM public.park_area WHERE id_parc_jardin = :parc_jardin_id))     AS ndvi_job,
       (SELECT count(*) FROM public.ndvi_observation
         WHERE park_area_id IN (SELECT id FROM public.park_area WHERE id_parc_jardin = :parc_jardin_id)) AS ndvi_observation,
       (SELECT count(*) FROM public.ndvi_result
         WHERE park_area_computed_id IN (SELECT id FROM public.park_area WHERE id_parc_jardin = :parc_jardin_id)) AS ndvi_result_area,
       (SELECT count(*) FROM public.ndvi_result WHERE parc_et_jardin_id = :parc_jardin_id)       AS ndvi_result_pj,
       (SELECT count(*) FROM public.parc_prefecture WHERE id_parc = :parc_jardin_id)             AS parc_prefecture,
       (SELECT count(*) FROM public.parc_photo WHERE parc_id = :parc_jardin_id)                  AS parc_photo;
\endif

\if :{?confirm}
SELECT (:'confirm' = 'DELETE') AS do_delete \gset
\if :do_delete
\echo '=== 1. Suppression dans UNE transaction (tout ou rien) ==='
BEGIN;

-- Bloque les écritures concurrentes (batch NDVI / calcul) pendant le nettoyage.
LOCK TABLE public.park_area, public.park_area_computed, public.ndvi_job,
           public.ndvi_observation, public.ndvi_result, public.park_entrance
      IN SHARE ROW EXCLUSIVE MODE;

-- Cible : park_area demandé, ou tous les park_area du parc_jardin demandé.
\if :{?park_area_id}
CREATE TEMP TABLE tmp_del_area ON COMMIT DROP AS
SELECT id FROM public.park_area WHERE id = :park_area_id;
\elif :{?parc_jardin_id}
CREATE TEMP TABLE tmp_del_area ON COMMIT DROP AS
SELECT id FROM public.park_area WHERE id_parc_jardin = :parc_jardin_id;
\endif

-- Enfants d'abord (ndvi_result référence park_area_computed).
DELETE FROM public.ndvi_result        WHERE park_area_computed_id IN (SELECT id FROM tmp_del_area);
\if :{?parc_jardin_id}
DELETE FROM public.ndvi_result        WHERE parc_et_jardin_id = :parc_jardin_id;
\endif
DELETE FROM public.ndvi_observation   WHERE park_area_id IN (SELECT id FROM tmp_del_area);
DELETE FROM public.ndvi_job           WHERE id IN (SELECT id FROM tmp_del_area);
DELETE FROM public.park_area_computed WHERE id IN (SELECT id FROM tmp_del_area);
DELETE FROM public.park_entrance      WHERE area_id IN (SELECT id FROM tmp_del_area);
DELETE FROM public.park_area          WHERE id IN (SELECT id FROM tmp_del_area);

\if :{?parc_jardin_id}
DELETE FROM public.parc_prefecture    WHERE id_parc = :parc_jardin_id;
DELETE FROM public.parc_photo         WHERE parc_id = :parc_jardin_id;
DELETE FROM public.parc_jardin        WHERE identifiant = :parc_jardin_id;
\endif

COMMIT;
\echo 'Suppression terminée. Relancer les scripts 10/11/12 pour vérifier.'
\else
\echo '=== confirm != DELETE : mode analyse seulement, aucune écriture ==='
\endif
\else
\echo '=== Pas de -v confirm=DELETE : mode analyse seulement, aucune écriture ==='
\endif
