-- ============================================================
-- Chargement massif des données IRIS 2020 dans iris_data
-- Source : iris_base-ic-evol-struct-pop-2020.CSV (INSEE)
-- Séparateur : ; | En-tête : 1 ligne | Encodage : UTF-8
--
-- Stratégie :
--   1. Table de staging avec les colonnes CSV exactes (préfixes P20_/C20_)
--   2. COPY FROM pour import massif (pg_load)
--   3. INSERT INTO iris_data avec mapping et annee=2020
--   4. Nettoyage de la table de staging
--
-- Usage :
--   psql -U <user> -d <dbname> \
--        -v csv_path="'/chemin/absolu/iris_base-ic-evol-struct-pop-2020.CSV'" \
--        -f load_iris_data_2020.sql
--
--   Ou directement dans psql :
--   \set csv_path '/chemin/absolu/iris_base-ic-evol-struct-pop-2020.CSV'
-- ============================================================

BEGIN;

-- -------------------------------------------------------
-- 1. Table de staging : colonnes CSV originales en TEXT
--    pour éviter tout rejet de type lors du COPY
-- -------------------------------------------------------
CREATE TEMP TABLE staging_iris_2020 (
    iris          TEXT,
    com           TEXT,
    typ_iris      TEXT,
    lab_iris      TEXT,
    -- population totale (préfixe P20_)
    p20_pop       TEXT,
    p20_pop0002   TEXT,
    p20_pop0305   TEXT,
    p20_pop0610   TEXT,
    p20_pop1117   TEXT,
    p20_pop1824   TEXT,
    p20_pop2539   TEXT,
    p20_pop4054   TEXT,
    p20_pop5564   TEXT,
    p20_pop6579   TEXT,
    p20_pop80p    TEXT,
    p20_pop0014   TEXT,
    p20_pop1529   TEXT,
    p20_pop3044   TEXT,
    p20_pop4559   TEXT,
    p20_pop6074   TEXT,
    p20_pop75p    TEXT,
    p20_pop0019   TEXT,
    p20_pop2064   TEXT,
    p20_pop65p    TEXT,
    -- hommes (préfixe P20_)
    p20_poph      TEXT,
    p20_h0014     TEXT,
    p20_h1529     TEXT,
    p20_h3044     TEXT,
    p20_h4559     TEXT,
    p20_h6074     TEXT,
    p20_h75p      TEXT,
    p20_h0019     TEXT,
    p20_h2064     TEXT,
    p20_h65p      TEXT,
    -- femmes (préfixe P20_)
    p20_popf      TEXT,
    p20_f0014     TEXT,
    p20_f1529     TEXT,
    p20_f3044     TEXT,
    p20_f4559     TEXT,
    p20_f6074     TEXT,
    p20_f75p      TEXT,
    p20_f0019     TEXT,
    p20_f2064     TEXT,
    p20_f65p      TEXT,
    -- CSP 15+ total (préfixe C20_)
    c20_pop15p      TEXT,
    c20_pop15p_cs1  TEXT,
    c20_pop15p_cs2  TEXT,
    c20_pop15p_cs3  TEXT,
    c20_pop15p_cs4  TEXT,
    c20_pop15p_cs5  TEXT,
    c20_pop15p_cs6  TEXT,
    c20_pop15p_cs7  TEXT,
    c20_pop15p_cs8  TEXT,
    -- CSP 15+ hommes (préfixe C20_)
    c20_h15p        TEXT,
    c20_h15p_cs1    TEXT,
    c20_h15p_cs2    TEXT,
    c20_h15p_cs3    TEXT,
    c20_h15p_cs4    TEXT,
    c20_h15p_cs5    TEXT,
    c20_h15p_cs6    TEXT,
    c20_h15p_cs7    TEXT,
    c20_h15p_cs8    TEXT,
    -- CSP 15+ femmes (préfixe C20_)
    c20_f15p        TEXT,
    c20_f15p_cs1    TEXT,
    c20_f15p_cs2    TEXT,
    c20_f15p_cs3    TEXT,
    c20_f15p_cs4    TEXT,
    c20_f15p_cs5    TEXT,
    c20_f15p_cs6    TEXT,
    c20_f15p_cs7    TEXT,
    c20_f15p_cs8    TEXT,
    -- nationalité / ménages (préfixe P20_)
    p20_pop_fr    TEXT,
    p20_pop_etr   TEXT,
    p20_pop_imm   TEXT,
    p20_pmen      TEXT,
    p20_phormen   TEXT
) ON COMMIT DROP;

-- -------------------------------------------------------
-- 2. COPY massif depuis le CSV
--    Adapter le chemin absolu ci-dessous ou utiliser
--    la variable psql :csv_path
-- -------------------------------------------------------
COPY staging_iris_2020 (
    iris, com, typ_iris, lab_iris,
    p20_pop, p20_pop0002, p20_pop0305, p20_pop0610, p20_pop1117, p20_pop1824,
    p20_pop2539, p20_pop4054, p20_pop5564, p20_pop6579, p20_pop80p,
    p20_pop0014, p20_pop1529, p20_pop3044, p20_pop4559, p20_pop6074, p20_pop75p,
    p20_pop0019, p20_pop2064, p20_pop65p,
    p20_poph, p20_h0014, p20_h1529, p20_h3044, p20_h4559, p20_h6074, p20_h75p,
    p20_h0019, p20_h2064, p20_h65p,
    p20_popf, p20_f0014, p20_f1529, p20_f3044, p20_f4559, p20_f6074, p20_f75p,
    p20_f0019, p20_f2064, p20_f65p,
    c20_pop15p, c20_pop15p_cs1, c20_pop15p_cs2, c20_pop15p_cs3, c20_pop15p_cs4,
    c20_pop15p_cs5, c20_pop15p_cs6, c20_pop15p_cs7, c20_pop15p_cs8,
    c20_h15p, c20_h15p_cs1, c20_h15p_cs2, c20_h15p_cs3, c20_h15p_cs4,
    c20_h15p_cs5, c20_h15p_cs6, c20_h15p_cs7, c20_h15p_cs8,
    c20_f15p, c20_f15p_cs1, c20_f15p_cs2, c20_f15p_cs3, c20_f15p_cs4,
    c20_f15p_cs5, c20_f15p_cs6, c20_f15p_cs7, c20_f15p_cs8,
    p20_pop_fr, p20_pop_etr, p20_pop_imm, p20_pmen, p20_phormen
)
FROM '/work/PERSO/ASSO/data/iris_base-ic-evol-struct-pop-2020.CSV'
WITH (
    FORMAT CSV,
    DELIMITER ';',
    HEADER TRUE,
    ENCODING 'UTF8',
    NULL ''
);

-- -------------------------------------------------------
-- 3. INSERT dans iris_data avec mapping des colonnes
--    annee = 2020 injecté en dur (issu du nom de fichier)
--    NULLIF(...,'') assure la conversion propre des vides
-- -------------------------------------------------------
INSERT INTO iris_data (
    annee,
    iris,
    com,
    typ_iris,
    lab_iris,
    pop,
    pop0002, pop0305, pop0610, pop1117, pop1824,
    pop2539, pop4054, pop5564, pop6579, pop80p,
    pop0014, pop1529, pop3044, pop4559, pop6074, pop75p,
    pop0019, pop2064, pop65p,
    poph,
    h0014, h1529, h3044, h4559, h6074, h75p,
    h0019, h2064, h65p,
    popf,
    f0014, f1529, f3044, f4559, f6074, f75p,
    f0019, f2064, f65p,
    pop15p,
    pop15p_cs1, pop15p_cs2, pop15p_cs3, pop15p_cs4,
    pop15p_cs5, pop15p_cs6, pop15p_cs7, pop15p_cs8,
    h15p,
    h15p_cs1, h15p_cs2, h15p_cs3, h15p_cs4,
    h15p_cs5, h15p_cs6, h15p_cs7, h15p_cs8,
    f15p,
    f15p_cs1, f15p_cs2, f15p_cs3, f15p_cs4,
    f15p_cs5, f15p_cs6, f15p_cs7, f15p_cs8,
    pop_fr, pop_etr, pop_imm,
    pmen, phormen
)
SELECT
    2020                                        AS annee,
    s.iris,
    s.com,
    s.typ_iris,
    s.lab_iris,
    NULLIF(s.p20_pop,     '')::NUMERIC(16,4)   AS pop,
    NULLIF(s.p20_pop0002, '')::NUMERIC(16,4)   AS pop0002,
    NULLIF(s.p20_pop0305, '')::NUMERIC(16,4)   AS pop0305,
    NULLIF(s.p20_pop0610, '')::NUMERIC(16,4)   AS pop0610,
    NULLIF(s.p20_pop1117, '')::NUMERIC(16,4)   AS pop1117,
    NULLIF(s.p20_pop1824, '')::NUMERIC(16,4)   AS pop1824,
    NULLIF(s.p20_pop2539, '')::NUMERIC(16,4)   AS pop2539,
    NULLIF(s.p20_pop4054, '')::NUMERIC(16,4)   AS pop4054,
    NULLIF(s.p20_pop5564, '')::NUMERIC(16,4)   AS pop5564,
    NULLIF(s.p20_pop6579, '')::NUMERIC(16,4)   AS pop6579,
    NULLIF(s.p20_pop80p,  '')::NUMERIC(16,4)   AS pop80p,
    NULLIF(s.p20_pop0014, '')::NUMERIC(16,4)   AS pop0014,
    NULLIF(s.p20_pop1529, '')::NUMERIC(16,4)   AS pop1529,
    NULLIF(s.p20_pop3044, '')::NUMERIC(16,4)   AS pop3044,
    NULLIF(s.p20_pop4559, '')::NUMERIC(16,4)   AS pop4559,
    NULLIF(s.p20_pop6074, '')::NUMERIC(16,4)   AS pop6074,
    NULLIF(s.p20_pop75p,  '')::NUMERIC(16,4)   AS pop75p,
    NULLIF(s.p20_pop0019, '')::NUMERIC(16,4)   AS pop0019,
    NULLIF(s.p20_pop2064, '')::NUMERIC(16,4)   AS pop2064,
    NULLIF(s.p20_pop65p,  '')::NUMERIC(16,4)   AS pop65p,
    NULLIF(s.p20_poph,    '')::NUMERIC(16,4)   AS poph,
    NULLIF(s.p20_h0014,   '')::NUMERIC(16,4)   AS h0014,
    NULLIF(s.p20_h1529,   '')::NUMERIC(16,4)   AS h1529,
    NULLIF(s.p20_h3044,   '')::NUMERIC(16,4)   AS h3044,
    NULLIF(s.p20_h4559,   '')::NUMERIC(16,4)   AS h4559,
    NULLIF(s.p20_h6074,   '')::NUMERIC(16,4)   AS h6074,
    NULLIF(s.p20_h75p,    '')::NUMERIC(16,4)   AS h75p,
    NULLIF(s.p20_h0019,   '')::NUMERIC(16,4)   AS h0019,
    NULLIF(s.p20_h2064,   '')::NUMERIC(16,4)   AS h2064,
    NULLIF(s.p20_h65p,    '')::NUMERIC(16,4)   AS h65p,
    NULLIF(s.p20_popf,    '')::NUMERIC(16,4)   AS popf,
    NULLIF(s.p20_f0014,   '')::NUMERIC(16,4)   AS f0014,
    NULLIF(s.p20_f1529,   '')::NUMERIC(16,4)   AS f1529,
    NULLIF(s.p20_f3044,   '')::NUMERIC(16,4)   AS f3044,
    NULLIF(s.p20_f4559,   '')::NUMERIC(16,4)   AS f4559,
    NULLIF(s.p20_f6074,   '')::NUMERIC(16,4)   AS f6074,
    NULLIF(s.p20_f75p,    '')::NUMERIC(16,4)   AS f75p,
    NULLIF(s.p20_f0019,   '')::NUMERIC(16,4)   AS f0019,
    NULLIF(s.p20_f2064,   '')::NUMERIC(16,4)   AS f2064,
    NULLIF(s.p20_f65p,    '')::NUMERIC(16,4)   AS f65p,
    NULLIF(s.c20_pop15p,     '')::NUMERIC(16,4) AS pop15p,
    NULLIF(s.c20_pop15p_cs1, '')::NUMERIC(16,4) AS pop15p_cs1,
    NULLIF(s.c20_pop15p_cs2, '')::NUMERIC(16,4) AS pop15p_cs2,
    NULLIF(s.c20_pop15p_cs3, '')::NUMERIC(16,4) AS pop15p_cs3,
    NULLIF(s.c20_pop15p_cs4, '')::NUMERIC(16,4) AS pop15p_cs4,
    NULLIF(s.c20_pop15p_cs5, '')::NUMERIC(16,4) AS pop15p_cs5,
    NULLIF(s.c20_pop15p_cs6, '')::NUMERIC(16,4) AS pop15p_cs6,
    NULLIF(s.c20_pop15p_cs7, '')::NUMERIC(16,4) AS pop15p_cs7,
    NULLIF(s.c20_pop15p_cs8, '')::NUMERIC(16,4) AS pop15p_cs8,
    NULLIF(s.c20_h15p,       '')::NUMERIC(16,4) AS h15p,
    NULLIF(s.c20_h15p_cs1,   '')::NUMERIC(16,4) AS h15p_cs1,
    NULLIF(s.c20_h15p_cs2,   '')::NUMERIC(16,4) AS h15p_cs2,
    NULLIF(s.c20_h15p_cs3,   '')::NUMERIC(16,4) AS h15p_cs3,
    NULLIF(s.c20_h15p_cs4,   '')::NUMERIC(16,4) AS h15p_cs4,
    NULLIF(s.c20_h15p_cs5,   '')::NUMERIC(16,4) AS h15p_cs5,
    NULLIF(s.c20_h15p_cs6,   '')::NUMERIC(16,4) AS h15p_cs6,
    NULLIF(s.c20_h15p_cs7,   '')::NUMERIC(16,4) AS h15p_cs7,
    NULLIF(s.c20_h15p_cs8,   '')::NUMERIC(16,4) AS h15p_cs8,
    NULLIF(s.c20_f15p,       '')::NUMERIC(16,4) AS f15p,
    NULLIF(s.c20_f15p_cs1,   '')::NUMERIC(16,4) AS f15p_cs1,
    NULLIF(s.c20_f15p_cs2,   '')::NUMERIC(16,4) AS f15p_cs2,
    NULLIF(s.c20_f15p_cs3,   '')::NUMERIC(16,4) AS f15p_cs3,
    NULLIF(s.c20_f15p_cs4,   '')::NUMERIC(16,4) AS f15p_cs4,
    NULLIF(s.c20_f15p_cs5,   '')::NUMERIC(16,4) AS f15p_cs5,
    NULLIF(s.c20_f15p_cs6,   '')::NUMERIC(16,4) AS f15p_cs6,
    NULLIF(s.c20_f15p_cs7,   '')::NUMERIC(16,4) AS f15p_cs7,
    NULLIF(s.c20_f15p_cs8,   '')::NUMERIC(16,4) AS f15p_cs8,
    NULLIF(s.p20_pop_fr,  '')::NUMERIC(16,4)   AS pop_fr,
    NULLIF(s.p20_pop_etr, '')::NUMERIC(16,4)   AS pop_etr,
    NULLIF(s.p20_pop_imm, '')::NUMERIC(16,4)   AS pop_imm,
    NULLIF(s.p20_pmen,    '')::NUMERIC(16,4)   AS pmen,
    NULLIF(s.p20_phormen, '')::NUMERIC(16,4)   AS phormen
FROM staging_iris_2020 s
ON CONFLICT (annee, iris) DO UPDATE SET
    com           = EXCLUDED.com,
    typ_iris      = EXCLUDED.typ_iris,
    lab_iris      = EXCLUDED.lab_iris,
    pop           = EXCLUDED.pop,
    pop0002       = EXCLUDED.pop0002,
    pop0305       = EXCLUDED.pop0305,
    pop0610       = EXCLUDED.pop0610,
    pop1117       = EXCLUDED.pop1117,
    pop1824       = EXCLUDED.pop1824,
    pop2539       = EXCLUDED.pop2539,
    pop4054       = EXCLUDED.pop4054,
    pop5564       = EXCLUDED.pop5564,
    pop6579       = EXCLUDED.pop6579,
    pop80p        = EXCLUDED.pop80p,
    pop0014       = EXCLUDED.pop0014,
    pop1529       = EXCLUDED.pop1529,
    pop3044       = EXCLUDED.pop3044,
    pop4559       = EXCLUDED.pop4559,
    pop6074       = EXCLUDED.pop6074,
    pop75p        = EXCLUDED.pop75p,
    pop0019       = EXCLUDED.pop0019,
    pop2064       = EXCLUDED.pop2064,
    pop65p        = EXCLUDED.pop65p,
    poph          = EXCLUDED.poph,
    h0014         = EXCLUDED.h0014,
    h1529         = EXCLUDED.h1529,
    h3044         = EXCLUDED.h3044,
    h4559         = EXCLUDED.h4559,
    h6074         = EXCLUDED.h6074,
    h75p          = EXCLUDED.h75p,
    h0019         = EXCLUDED.h0019,
    h2064         = EXCLUDED.h2064,
    h65p          = EXCLUDED.h65p,
    popf          = EXCLUDED.popf,
    f0014         = EXCLUDED.f0014,
    f1529         = EXCLUDED.f1529,
    f3044         = EXCLUDED.f3044,
    f4559         = EXCLUDED.f4559,
    f6074         = EXCLUDED.f6074,
    f75p          = EXCLUDED.f75p,
    f0019         = EXCLUDED.f0019,
    f2064         = EXCLUDED.f2064,
    f65p          = EXCLUDED.f65p,
    pop15p        = EXCLUDED.pop15p,
    pop15p_cs1    = EXCLUDED.pop15p_cs1,
    pop15p_cs2    = EXCLUDED.pop15p_cs2,
    pop15p_cs3    = EXCLUDED.pop15p_cs3,
    pop15p_cs4    = EXCLUDED.pop15p_cs4,
    pop15p_cs5    = EXCLUDED.pop15p_cs5,
    pop15p_cs6    = EXCLUDED.pop15p_cs6,
    pop15p_cs7    = EXCLUDED.pop15p_cs7,
    pop15p_cs8    = EXCLUDED.pop15p_cs8,
    h15p          = EXCLUDED.h15p,
    h15p_cs1      = EXCLUDED.h15p_cs1,
    h15p_cs2      = EXCLUDED.h15p_cs2,
    h15p_cs3      = EXCLUDED.h15p_cs3,
    h15p_cs4      = EXCLUDED.h15p_cs4,
    h15p_cs5      = EXCLUDED.h15p_cs5,
    h15p_cs6      = EXCLUDED.h15p_cs6,
    h15p_cs7      = EXCLUDED.h15p_cs7,
    h15p_cs8      = EXCLUDED.h15p_cs8,
    f15p          = EXCLUDED.f15p,
    f15p_cs1      = EXCLUDED.f15p_cs1,
    f15p_cs2      = EXCLUDED.f15p_cs2,
    f15p_cs3      = EXCLUDED.f15p_cs3,
    f15p_cs4      = EXCLUDED.f15p_cs4,
    f15p_cs5      = EXCLUDED.f15p_cs5,
    f15p_cs6      = EXCLUDED.f15p_cs6,
    f15p_cs7      = EXCLUDED.f15p_cs7,
    f15p_cs8      = EXCLUDED.f15p_cs8,
    pop_fr        = EXCLUDED.pop_fr,
    pop_etr       = EXCLUDED.pop_etr,
    pop_imm       = EXCLUDED.pop_imm,
    pmen          = EXCLUDED.pmen,
    phormen       = EXCLUDED.phormen;

-- -------------------------------------------------------
-- 4. Vérification rapide
-- -------------------------------------------------------
DO $$
DECLARE
    nb_rows BIGINT;
BEGIN
    SELECT COUNT(*) INTO nb_rows FROM iris_data WHERE annee = 2020;
    RAISE NOTICE 'Chargement 2020 terminé : % lignes dans iris_data', nb_rows;
END $$;

COMMIT;
