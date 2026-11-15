-- Détection 2 : park_area qui se chevauchent (doublons probables). LECTURE SEULE.
-- Aucune écriture. À exécuter :
--   psql "$DB_URL" -f scripts/sql/11_park_area_chevauchements.sql
-- Seuil : 50 % de la plus petite surface. Modifier 0.5 si besoin.
\set ON_ERROR_STOP on
\pset pager off

\echo '=== 2a. park_area invalides (ST_IsValid = false), exclus du test de chevauchement ==='
SELECT a.id, a.name, a.id_parc_jardin,
       c.name AS city, cc.name AS epci,
       ST_IsValidReason(a.polygon) AS raison
FROM public.park_area a
LEFT JOIN public.parc_jardin pj      ON pj.identifiant = a.id_parc_jardin
LEFT JOIN public.city c              ON c.id = pj.id_city
LEFT JOIN public.adm_com2commune cc  ON cc.id = c.id_comm2co
WHERE a.polygon IS NOT NULL AND NOT ST_IsValid(a.polygon)
ORDER BY a.id;

\echo '=== 2b. paires de park_area dont les polygones se chevauchent (>= 50 %) ==='
-- Périodes normalisées : date_debut absente = 1900-01-01, date_fin absente =
-- 2199-12-31. Les paires dont les périodes sont disjointes (restructuration)
-- sont écartées ici et listées en 2b-bis.
WITH pa AS (
  SELECT a.id, a.name, a.id_parc_jardin, a.polygon,
         coalesce(pj.date_debut, '1900-01-01') AS d_debut,
         coalesce(pj.date_fin,   '2199-12-31') AS d_fin,
         pj.date_debut, pj.date_fin,
         c.name AS city, cc.name AS epci
  FROM public.park_area a
  LEFT JOIN public.parc_jardin pj      ON pj.identifiant = a.id_parc_jardin
  LEFT JOIN public.city c              ON c.id = pj.id_city
  LEFT JOIN public.adm_com2commune cc  ON cc.id = c.id_comm2co
  WHERE a.polygon IS NOT NULL AND ST_IsValid(a.polygon)
)
SELECT a.id              AS area_a_id,
       a.name            AS area_a_name,
       a.id_parc_jardin  AS area_a_parc_jardin,
       a.date_debut      AS area_a_date_debut,
       a.date_fin        AS area_a_date_fin,
       a.city            AS area_a_city,
       a.epci            AS area_a_epci,
       b.id              AS area_b_id,
       b.name            AS area_b_name,
       b.id_parc_jardin  AS area_b_parc_jardin,
       b.date_debut      AS area_b_date_debut,
       b.date_fin        AS area_b_date_fin,
       b.city            AS area_b_city,
       b.epci            AS area_b_epci,
       round((100.0 * ST_Area(ST_Intersection(a.polygon, b.polygon))
             / GREATEST(LEAST(ST_Area(a.polygon), ST_Area(b.polygon)), 1e-12))::numeric, 1) AS pct_petit_recouvert,
       round((100.0 * ST_Area(ST_Intersection(a.polygon, b.polygon))
             / GREATEST(ST_Area(ST_Union(a.polygon, b.polygon)), 1e-12))::numeric, 1) AS pct_iou
FROM pa a
JOIN pa b ON a.id < b.id
WHERE ST_Intersects(a.polygon, b.polygon)
  AND ST_Area(ST_Intersection(a.polygon, b.polygon))
      >= 0.5 * LEAST(ST_Area(a.polygon), ST_Area(b.polygon))
  AND a.d_fin >= b.d_debut
  AND b.d_fin >= a.d_debut
ORDER BY pct_petit_recouvert DESC, a.id, b.id
LIMIT 500;

\echo '=== 2b-bis. paires écartées : périodes disjointes (restructuration, pas un doublon) ==='
WITH pa AS (
  SELECT a.id, a.name, a.id_parc_jardin, a.polygon,
         coalesce(pj.date_debut, '1900-01-01') AS d_debut,
         coalesce(pj.date_fin,   '2199-12-31') AS d_fin,
         pj.date_debut, pj.date_fin
  FROM public.park_area a
  LEFT JOIN public.parc_jardin pj      ON pj.identifiant = a.id_parc_jardin
  WHERE a.polygon IS NOT NULL AND ST_IsValid(a.polygon)
)
SELECT a.id AS area_a_id, a.name AS area_a_name,
       a.date_debut AS area_a_date_debut, a.date_fin AS area_a_date_fin,
       b.id AS area_b_id, b.name AS area_b_name,
       b.date_debut AS area_b_date_debut, b.date_fin AS area_b_date_fin,
       round((100.0 * ST_Area(ST_Intersection(a.polygon, b.polygon))
             / GREATEST(LEAST(ST_Area(a.polygon), ST_Area(b.polygon)), 1e-12))::numeric, 1) AS pct_petit_recouvert
FROM pa a
JOIN pa b ON a.id < b.id
WHERE ST_Intersects(a.polygon, b.polygon)
  AND ST_Area(ST_Intersection(a.polygon, b.polygon))
      >= 0.5 * LEAST(ST_Area(a.polygon), ST_Area(b.polygon))
  AND (a.d_fin < b.d_debut OR b.d_fin < a.d_debut)
ORDER BY pct_petit_recouvert DESC, a.id, b.id
LIMIT 500;

\echo '=== 2c. synthèse : park_area en doublon par nom (rappel 1c) ==='
WITH n AS (
  SELECT a.id, a.name, pj.date_debut, pj.date_fin,
         coalesce(pj.date_debut, '1900-01-01') AS d_debut,
         coalesce(pj.date_fin,   '2199-12-31') AS d_fin,
         c.name AS city, cc.name AS epci,
         regexp_replace(lower(trim(a.name)), '\s+', ' ', 'g') AS name_key
  FROM public.park_area a
  LEFT JOIN public.parc_jardin pj      ON pj.identifiant = a.id_parc_jardin
  LEFT JOIN public.city c              ON c.id = pj.id_city
  LEFT JOIN public.adm_com2commune cc  ON cc.id = c.id_comm2co
)
SELECT name_key, count(*) AS nb, array_agg(id ORDER BY id) AS park_area_ids,
       array_agg(date_debut ORDER BY id) AS date_debuts,
       array_agg(date_fin   ORDER BY id) AS date_fins,
       array_agg(DISTINCT city) AS cities, array_agg(DISTINCT epci) AS epcis
FROM n
WHERE name_key <> ''
GROUP BY name_key
HAVING count(*) > 1
   -- Groupes de périodes disjointes = restructurations, écartés (cf. 1c-bis).
   AND max(d_debut) <= min(d_fin)
ORDER BY nb DESC, name_key
LIMIT 200;
