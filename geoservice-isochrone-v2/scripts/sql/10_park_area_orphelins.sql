-- Détection 1 : park_area orphelins ou suspects (LECTURE SEULE).
-- Aucune écriture. À exécuter :
--   psql "$DB_URL" -f scripts/sql/10_park_area_orphelins.sql
\set ON_ERROR_STOP on
\pset pager off

\echo '=== 1a. park_area sans id_parc_jardin (NULL) ==='
SELECT a.id, a.name, pj.nom_parc, c.name AS city, cc.name AS epci, a.updated
FROM public.park_area a
LEFT JOIN public.parc_jardin pj      ON pj.identifiant = a.id_parc_jardin
LEFT JOIN public.city c              ON c.id = pj.id_city
LEFT JOIN public.adm_com2commune cc  ON cc.id = c.id_comm2co
WHERE a.id_parc_jardin IS NULL
ORDER BY a.name, a.id;

\echo '=== 1b. park_area dont id_parc_jardin ne pointe vers aucun parc_jardin ==='
SELECT a.id, a.name, a.id_parc_jardin, a.updated
FROM public.park_area a
LEFT JOIN public.parc_jardin pj ON pj.identifiant = a.id_parc_jardin
WHERE a.id_parc_jardin IS NOT NULL
  AND pj.identifiant IS NULL
ORDER BY a.name, a.id;

\echo '=== 1c. park_area homonymes (même nom normalisé, plusieurs lignes) ==='
-- date_debut / date_fin : une restructuration (parc fermé puis remplacé par un
-- autre portant le même nom) produit deux lignes homonymes légitimes.
WITH n AS (
  SELECT a.id, a.name, a.id_parc_jardin, a.updated,
         pj.date_debut, pj.date_fin,
         coalesce(pj.date_debut, '1900-01-01') AS d_debut,
         coalesce(pj.date_fin,   '2199-12-31') AS d_fin,
         c.name  AS city, cc.name AS epci,
         regexp_replace(lower(trim(a.name)), '\s+', ' ', 'g') AS name_key
  FROM public.park_area a
  LEFT JOIN public.parc_jardin pj      ON pj.identifiant = a.id_parc_jardin
  LEFT JOIN public.city c              ON c.id = pj.id_city
  LEFT JOIN public.adm_com2commune cc  ON cc.id = c.id_comm2co
)
SELECT n.name_key,
       count(*)                       AS nb_park_area,
       array_agg(n.id ORDER BY n.id)  AS park_area_ids,
       array_agg(n.id_parc_jardin ORDER BY n.id) AS parc_jardin_ids,
       array_agg(n.date_debut ORDER BY n.id) AS date_debuts,
       array_agg(n.date_fin   ORDER BY n.id) AS date_fins,
       array_agg(DISTINCT n.city)     AS cities,
       array_agg(DISTINCT n.epci)     AS epcis,
       min(n.name)                    AS nom
FROM n
WHERE n.name_key <> ''
GROUP BY n.name_key
HAVING count(*) > 1
   -- Au moins deux périodes se recouvrent (sinon : restructuration, cf. 1c-bis).
   AND max(n.d_debut) <= min(n.d_fin)
ORDER BY nb_park_area DESC, nom
LIMIT 200;

\echo '=== 1c-bis. homonymes écartés : périodes disjointes (restructuration) ==='
WITH n AS (
  SELECT a.id, a.name,
         coalesce(pj.date_debut, '1900-01-01') AS d_debut,
         coalesce(pj.date_fin,   '2199-12-31') AS d_fin,
         pj.date_debut, pj.date_fin,
         regexp_replace(lower(trim(a.name)), '\s+', ' ', 'g') AS name_key
  FROM public.park_area a
  LEFT JOIN public.parc_jardin pj ON pj.identifiant = a.id_parc_jardin
)
SELECT n.name_key,
       count(*)                       AS nb_park_area,
       array_agg(n.id ORDER BY n.id)  AS park_area_ids,
       array_agg(n.date_debut ORDER BY n.id) AS date_debuts,
       array_agg(n.date_fin   ORDER BY n.id) AS date_fins
FROM n
WHERE n.name_key <> ''
GROUP BY n.name_key
HAVING count(*) > 1
   AND max(n.d_debut) > min(n.d_fin)
ORDER BY nb_park_area DESC, name_key
LIMIT 200;

\echo '=== 1f. dates de parc_jardin incohérentes (restructurations, saisies erronées) ==='
-- Ces lignes rendent l'analyse des doublons peu fiable : à corriger avant
-- de conclure qu'un homonyme est un doublon. Plage considérée plausible :
-- 1900 .. année courante.
WITH d AS (
  SELECT pj.identifiant, pj.nom_parc, pj.date_debut, pj.date_fin, pj.date_suppr,
         c.name AS city, cc.name AS epci,
         CASE
           WHEN pj.date_debut IS NOT NULL AND pj.date_fin IS NOT NULL
                AND pj.date_fin < pj.date_debut
             THEN 'date_fin < date_debut'
           WHEN pj.date_fin IS NOT NULL AND pj.date_debut IS NULL
             THEN 'date_fin sans date_debut'
           WHEN date_part('year', pj.date_debut) NOT BETWEEN 1900 AND date_part('year', now())
             THEN 'date_debut hors plage 1900..année courante'
           WHEN date_part('year', pj.date_fin) NOT BETWEEN 1900 AND date_part('year', now())
             THEN 'date_fin hors plage 1900..année courante'
           WHEN pj.date_suppr IS NOT NULL
             THEN 'date_suppr renseignée (parc supprimé)'
         END AS probleme
  FROM public.parc_jardin pj
  LEFT JOIN public.city c              ON c.id = pj.id_city
  LEFT JOIN public.adm_com2commune cc  ON cc.id = c.id_comm2co
)
SELECT identifiant, nom_parc, date_debut, date_fin, date_suppr, city, epci, probleme
FROM d
WHERE probleme IS NOT NULL
ORDER BY probleme, identifiant
LIMIT 500;

\echo '=== 1d. park_area / parc_jardin : nom identique mais ids différents ==='
-- Périodes normalisées : date_debut absente = 1900-01-01, date_fin absente =
-- 2199-12-31. Les paires de périodes disjointes (restructuration) sont
-- écartées ici et listées en 1d-bis.
SELECT a.id AS park_area_id, a.name AS park_area_name,
       a.id_parc_jardin, pja.date_debut AS pa_date_debut, pja.date_fin AS pa_date_fin,
       pj.identifiant AS parc_jardin_id, pj.nom_parc,
       pj.date_debut, pj.date_fin,
       c.name AS city, cc.name AS epci
FROM public.park_area a
JOIN public.parc_jardin pj ON regexp_replace(lower(trim(pj.nom_parc)), '\s+', ' ', 'g')
                           = regexp_replace(lower(trim(a.name)), '\s+', ' ', 'g')
LEFT JOIN public.parc_jardin pja     ON pja.identifiant = a.id_parc_jardin
LEFT JOIN public.city c              ON c.id = pj.id_city
LEFT JOIN public.adm_com2commune cc  ON cc.id = c.id_comm2co
WHERE pj.identifiant IS DISTINCT FROM a.id_parc_jardin
  AND coalesce(pj.date_fin, '2199-12-31') >= coalesce(pja.date_debut, '1900-01-01')
  AND coalesce(pja.date_fin, '2199-12-31') >= coalesce(pj.date_debut, '1900-01-01')
ORDER BY a.name, a.id
LIMIT 200;

\echo '=== 1d-bis. paires écartées : périodes disjointes (restructuration, pas un doublon) ==='
SELECT a.id AS park_area_id, a.name AS park_area_name,
       a.id_parc_jardin, pja.date_debut AS pa_date_debut, pja.date_fin AS pa_date_fin,
       pj.identifiant AS parc_jardin_id, pj.nom_parc,
       pj.date_debut, pj.date_fin,
       c.name AS city, cc.name AS epci
FROM public.park_area a
JOIN public.parc_jardin pj ON regexp_replace(lower(trim(pj.nom_parc)), '\s+', ' ', 'g')
                           = regexp_replace(lower(trim(a.name)), '\s+', ' ', 'g')
LEFT JOIN public.parc_jardin pja     ON pja.identifiant = a.id_parc_jardin
LEFT JOIN public.city c              ON c.id = pj.id_city
LEFT JOIN public.adm_com2commune cc  ON cc.id = c.id_comm2co
WHERE pj.identifiant IS DISTINCT FROM a.id_parc_jardin
  AND (coalesce(pj.date_fin, '2199-12-31') < coalesce(pja.date_debut, '1900-01-01')
       OR coalesce(pja.date_fin, '2199-12-31') < coalesce(pj.date_debut, '1900-01-01'))
ORDER BY a.name, a.id
LIMIT 200;

\echo '=== 1e. parc_jardin sans park_area (pour information) ==='
SELECT pj.identifiant, pj.nom_parc, c.name AS city, cc.name AS epci,
       pj.date_debut, pj.date_fin, pj.date_suppr
FROM public.parc_jardin pj
LEFT JOIN public.park_area a         ON a.id_parc_jardin = pj.identifiant
LEFT JOIN public.city c              ON c.id = pj.id_city
LEFT JOIN public.adm_com2commune cc  ON cc.id = c.id_comm2co
WHERE a.id IS NULL
ORDER BY pj.nom_parc
LIMIT 200;
