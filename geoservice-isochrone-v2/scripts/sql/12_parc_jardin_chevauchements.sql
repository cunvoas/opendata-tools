-- Détection 3 : parc_jardin doublons / contours qui se chevauchent. LECTURE SEULE.
-- Aucune écriture. À exécuter :
--   psql "$DB_URL" -f scripts/sql/12_parc_jardin_chevauchements.sql
-- Seuil : 50 % de la plus petite surface. Modifier 0.5 si besoin.
\set ON_ERROR_STOP on
\pset pager off

\echo '=== 3a. parc_jardin homonymes (même nom normalisé, plusieurs identifiants) ==='
-- date_debut / date_fin : deux homonymes successifs (parc fermé, puis
-- remplacé) sont une restructuration, pas un doublon.
WITH n AS (
  SELECT pj.identifiant, pj.nom_parc, pj.id_city, pj.date_debut, pj.date_fin, pj.date_suppr,
         coalesce(pj.date_debut, '1900-01-01') AS d_debut,
         coalesce(pj.date_fin,   '2199-12-31') AS d_fin,
         c.name AS city, cc.name AS epci,
         regexp_replace(lower(trim(pj.nom_parc)), '\s+', ' ', 'g') AS name_key
  FROM public.parc_jardin pj
  LEFT JOIN public.city c              ON c.id = pj.id_city
  LEFT JOIN public.adm_com2commune cc  ON cc.id = c.id_comm2co
)
SELECT name_key,
       count(*)                                    AS nb,
       array_agg(identifiant ORDER BY identifiant) AS parc_jardin_ids,
       array_agg(date_debut ORDER BY identifiant)  AS date_debuts,
       array_agg(date_fin   ORDER BY identifiant)  AS date_fins,
       array_agg(DISTINCT city)                    AS cities,
       array_agg(DISTINCT epci)                    AS epcis,
       min(nom_parc)                               AS nom
FROM n
WHERE name_key <> ''
GROUP BY name_key
HAVING count(*) > 1
   -- Groupes de périodes disjointes = restructurations, écartés (cf. 3a-bis).
   AND max(d_debut) <= min(d_fin)
ORDER BY nb DESC, nom
LIMIT 200;

\echo '=== 3a-bis. homonymes écartés : périodes disjointes (restructuration) ==='
WITH n AS (
  SELECT pj.identifiant, pj.nom_parc, pj.date_debut, pj.date_fin,
         coalesce(pj.date_debut, '1900-01-01') AS d_debut,
         coalesce(pj.date_fin,   '2199-12-31') AS d_fin,
         regexp_replace(lower(trim(pj.nom_parc)), '\s+', ' ', 'g') AS name_key
  FROM public.parc_jardin pj
)
SELECT name_key,
       count(*)                                    AS nb,
       array_agg(identifiant ORDER BY identifiant) AS parc_jardin_ids,
       array_agg(date_debut ORDER BY identifiant)  AS date_debuts,
       array_agg(date_fin   ORDER BY identifiant)  AS date_fins
FROM n
WHERE name_key <> ''
GROUP BY name_key
HAVING count(*) > 1
   AND max(d_debut) > min(d_fin)
ORDER BY nb DESC, name_key
LIMIT 200;

\echo '=== 3b. paires de parc_jardin dont les contours se chevauchent (>= 50 %) ==='
-- Périodes normalisées : date_debut absente = 1900-01-01, date_fin absente =
-- 2199-12-31. Les paires dont les périodes sont disjointes (restructuration)
-- sont écartées ici et listées en 3b-bis.
WITH pj AS (
  SELECT p.identifiant, p.nom_parc, p.id_city, p.contour, p.date_debut, p.date_fin,
         coalesce(p.date_debut, '1900-01-01') AS d_debut,
         coalesce(p.date_fin,   '2199-12-31') AS d_fin,
         c.name AS city, cc.name AS epci
  FROM public.parc_jardin p
  LEFT JOIN public.city c              ON c.id = p.id_city
  LEFT JOIN public.adm_com2commune cc  ON cc.id = c.id_comm2co
  WHERE p.contour IS NOT NULL AND ST_IsValid(p.contour)
)
SELECT a.identifiant      AS parc_a_id,
       a.nom_parc         AS parc_a_name,
       a.date_debut       AS parc_a_date_debut,
       a.date_fin         AS parc_a_date_fin,
       a.city             AS parc_a_city,
       a.epci             AS parc_a_epci,
       b.identifiant      AS parc_b_id,
       b.nom_parc         AS parc_b_name,
       b.date_debut       AS parc_b_date_debut,
       b.date_fin         AS parc_b_date_fin,
       b.city             AS parc_b_city,
       b.epci             AS parc_b_epci,
       round((100.0 * ST_Area(ST_Intersection(a.contour, b.contour))
             / GREATEST(LEAST(ST_Area(a.contour), ST_Area(b.contour)), 1e-12))::numeric, 1) AS pct_petit_recouvert,
       round((100.0 * ST_Area(ST_Intersection(a.contour, b.contour))
             / GREATEST(ST_Area(ST_Union(a.contour, b.contour)), 1e-12))::numeric, 1) AS pct_iou
FROM pj a
JOIN pj b ON a.identifiant < b.identifiant
WHERE ST_Intersects(a.contour, b.contour)
  AND ST_Area(ST_Intersection(a.contour, b.contour))
      >= 0.5 * LEAST(ST_Area(a.contour), ST_Area(b.contour))
  AND a.d_fin >= b.d_debut
  AND b.d_fin >= a.d_debut
ORDER BY pct_petit_recouvert DESC, a.identifiant, b.identifiant
LIMIT 500;

\echo '=== 3b-bis. paires écartées : périodes disjointes (restructuration, pas un doublon) ==='
WITH pj AS (
  SELECT p.identifiant, p.nom_parc, p.contour, p.date_debut, p.date_fin,
         coalesce(p.date_debut, '1900-01-01') AS d_debut,
         coalesce(p.date_fin,   '2199-12-31') AS d_fin
  FROM public.parc_jardin p
  WHERE p.contour IS NOT NULL AND ST_IsValid(p.contour)
)
SELECT a.identifiant AS parc_a_id, a.nom_parc AS parc_a_name,
       a.date_debut AS parc_a_date_debut, a.date_fin AS parc_a_date_fin,
       b.identifiant AS parc_b_id, b.nom_parc AS parc_b_name,
       b.date_debut AS parc_b_date_debut, b.date_fin AS parc_b_date_fin,
       round((100.0 * ST_Area(ST_Intersection(a.contour, b.contour))
             / GREATEST(LEAST(ST_Area(a.contour), ST_Area(b.contour)), 1e-12))::numeric, 1) AS pct_petit_recouvert
FROM pj a
JOIN pj b ON a.identifiant < b.identifiant
WHERE ST_Intersects(a.contour, b.contour)
  AND ST_Area(ST_Intersection(a.contour, b.contour))
      >= 0.5 * LEAST(ST_Area(a.contour), ST_Area(b.contour))
  AND (a.d_fin < b.d_debut OR b.d_fin < a.d_debut)
ORDER BY pct_petit_recouvert DESC, a.identifiant, b.identifiant
LIMIT 500;

\echo '=== 3c. parc_jardin avec coordonnée identique (même point, ids différents) ==='
-- Paires de périodes disjointes écartées (restructuration).
SELECT a.identifiant AS parc_a_id, a.nom_parc AS parc_a_name,
       a.date_debut AS parc_a_date_debut, a.date_fin AS parc_a_date_fin,
       ca.name AS parc_a_city, cca.name AS parc_a_epci,
       b.identifiant AS parc_b_id, b.nom_parc AS parc_b_name,
       b.date_debut AS parc_b_date_debut, b.date_fin AS parc_b_date_fin,
       cb.name AS parc_b_city, ccb.name AS parc_b_epci,
       ST_AsText(a.coordonnee) AS point
FROM public.parc_jardin a
JOIN public.parc_jardin b             ON a.identifiant < b.identifiant
LEFT JOIN public.city ca              ON ca.id = a.id_city
LEFT JOIN public.adm_com2commune cca  ON cca.id = ca.id_comm2co
LEFT JOIN public.city cb              ON cb.id = b.id_city
LEFT JOIN public.adm_com2commune ccb  ON ccb.id = cb.id_comm2co
WHERE a.coordonnee IS NOT NULL
  AND ST_Equals(a.coordonnee, b.coordonnee)
  AND coalesce(a.date_fin, '2199-12-31') >= coalesce(b.date_debut, '1900-01-01')
  AND coalesce(b.date_fin, '2199-12-31') >= coalesce(a.date_debut, '1900-01-01')
ORDER BY a.nom_parc, a.identifiant
LIMIT 200;
