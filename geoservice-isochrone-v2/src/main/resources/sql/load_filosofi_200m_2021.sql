-- ============================================================
-- Chargement massif Filosofi 2021 — carreaux 200 m
-- Table cible : filosofi_200m (partition 2021)
--
-- Source INSEE : Filosofi2021_carreaux_200m_csv/
--   carreaux_200m_met.csv   → Métropole      (~2 298 582 lignes)
--   carreaux_200m_mart.csv  → Martinique     (~11 222 lignes)
--   carreaux_200m_reun.csv  → La Réunion     (~14 773 lignes)
--
-- Séparateur : ,  | En-tête : oui | Encodage : ASCII/UTF-8
-- Différence 2021 vs 2017/2019 : colonne "groupe" absente du CSV
--
-- Stratégie :
--   Réutilise la table de staging filosofi_load (existante)
--   en spécifiant explicitement la liste des colonnes CSV.
--   L'annee=2021 est injectée lors du INSERT.
--   Chaque fichier régional est chargé séquentiellement.
--
-- Prérequis :
--   - Partition filosofi_200m_2021 créée (partition_filosofi_200m_2021.sql)
--   - Table filosofi_load existante (filosofil.sql)
--
-- Usage :
--   psql -U <user> -d <dbname> -f load_filosofi_200m_2021.sql
-- ============================================================

-- Liste des colonnes CSV 2021 (sans "groupe" qui a disparu du millésime 2021)
\set CSV_COLS 'idcar_200m,idcar_1km,idcar_nat,i_est_200,i_est_1km,lcog_geo,ind,men,men_pauv,men_1ind,men_5ind,men_prop,men_fmp,ind_snv,men_surf,men_coll,men_mais,log_av45,log_45_70,log_70_90,log_ap90,log_inc,log_soc,ind_0_3,ind_4_5,ind_6_10,ind_11_17,ind_18_24,ind_25_39,ind_40_54,ind_55_64,ind_65_79,ind_80p,ind_inc'

-- Répertoire source (adapter si nécessaire)
\set DATA_DIR '/work/PERSO/ASSO/data/INSEE/Filosofi2021_carreaux_200m_csv'

BEGIN;

-- -------------------------------------------------------
-- Sécurité : vider uniquement les lignes 2021 existantes
--            pour permettre les relances idempotentes
-- -------------------------------------------------------
DELETE FROM filosofi_200m WHERE annee = 2021;


-- ═══════════════════════════════════════════════════════
-- FICHIER 1 : Métropole
-- ═══════════════════════════════════════════════════════
TRUNCATE public.filosofi_load;

COPY public.filosofi_load (
    idcar_200m, idcar_1km, idcar_nat, i_est_200, i_est_1km, lcog_geo,
    ind, men, men_pauv, men_1ind, men_5ind, men_prop, men_fmp,
    ind_snv, men_surf, men_coll, men_mais,
    log_av45, log_45_70, log_70_90, log_ap90, log_inc, log_soc,
    ind_0_3, ind_4_5, ind_6_10, ind_11_17, ind_18_24,
    ind_25_39, ind_40_54, ind_55_64, ind_65_79, ind_80p, ind_inc
)
FROM '/work/PERSO/ASSO/data/INSEE/Filosofi2021_carreaux_200m_csv/carreaux_200m_met.csv'
WITH (
    FORMAT CSV,
    DELIMITER ',',
    HEADER TRUE,
    ENCODING 'UTF8',
    FORCE_NOT_NULL (idcar_200m, idcar_1km, idcar_nat)
);

INSERT INTO public.filosofi_200m (
    annee,
    idcar_200m, i_est_200, idcar_1km, i_est_1km, idcar_nat,
    groupe,
    ind, men_1ind, men_5ind, men_prop, men_fmp,
    ind_snv, men_surf, men_coll, men_mais,
    log_av45, log_45_70, log_70_90, log_ap90, log_inc, log_soc,
    ind_0_3, ind_4_5, ind_6_10, ind_11_17, ind_18_24,
    ind_25_39, ind_40_54, ind_55_64, ind_65_79, ind_80p, ind_inc,
    men_pauv, men, lcog_geo
)
SELECT
    2021,
    idcar_200m, i_est_200, idcar_1km, i_est_1km, idcar_nat,
    groupe,          -- NULL pour 2021 (absent du CSV)
    ind, men_1ind, men_5ind, men_prop, men_fmp,
    ind_snv, men_surf, men_coll, men_mais,
    log_av45, log_45_70, log_70_90, log_ap90, log_inc, log_soc,
    ind_0_3, ind_4_5, ind_6_10, ind_11_17, ind_18_24,
    ind_25_39, ind_40_54, ind_55_64, ind_65_79, ind_80p, ind_inc,
    men_pauv, men, lcog_geo
FROM public.filosofi_load;

DO $$
DECLARE nb BIGINT;
BEGIN
    SELECT COUNT(*) INTO nb FROM public.filosofi_load;
    RAISE NOTICE '[MET] % carreaux chargés depuis carreaux_200m_met.csv', nb;
END $$;


-- ═══════════════════════════════════════════════════════
-- FICHIER 2 : Martinique
-- ═══════════════════════════════════════════════════════
TRUNCATE public.filosofi_load;

COPY public.filosofi_load (
    idcar_200m, idcar_1km, idcar_nat, i_est_200, i_est_1km, lcog_geo,
    ind, men, men_pauv, men_1ind, men_5ind, men_prop, men_fmp,
    ind_snv, men_surf, men_coll, men_mais,
    log_av45, log_45_70, log_70_90, log_ap90, log_inc, log_soc,
    ind_0_3, ind_4_5, ind_6_10, ind_11_17, ind_18_24,
    ind_25_39, ind_40_54, ind_55_64, ind_65_79, ind_80p, ind_inc
)
FROM '/work/PERSO/ASSO/data/INSEE/Filosofi2021_carreaux_200m_csv/carreaux_200m_mart.csv'
WITH (
    FORMAT CSV,
    DELIMITER ',',
    HEADER TRUE,
    ENCODING 'UTF8',
    FORCE_NOT_NULL (idcar_200m, idcar_1km, idcar_nat)
);

INSERT INTO public.filosofi_200m (
    annee,
    idcar_200m, i_est_200, idcar_1km, i_est_1km, idcar_nat,
    groupe,
    ind, men_1ind, men_5ind, men_prop, men_fmp,
    ind_snv, men_surf, men_coll, men_mais,
    log_av45, log_45_70, log_70_90, log_ap90, log_inc, log_soc,
    ind_0_3, ind_4_5, ind_6_10, ind_11_17, ind_18_24,
    ind_25_39, ind_40_54, ind_55_64, ind_65_79, ind_80p, ind_inc,
    men_pauv, men, lcog_geo
)
SELECT
    2021,
    idcar_200m, i_est_200, idcar_1km, i_est_1km, idcar_nat,
    groupe,
    ind, men_1ind, men_5ind, men_prop, men_fmp,
    ind_snv, men_surf, men_coll, men_mais,
    log_av45, log_45_70, log_70_90, log_ap90, log_inc, log_soc,
    ind_0_3, ind_4_5, ind_6_10, ind_11_17, ind_18_24,
    ind_25_39, ind_40_54, ind_55_64, ind_65_79, ind_80p, ind_inc,
    men_pauv, men, lcog_geo
FROM public.filosofi_load;

DO $$
DECLARE nb BIGINT;
BEGIN
    SELECT COUNT(*) INTO nb FROM public.filosofi_load;
    RAISE NOTICE '[MART] % carreaux chargés depuis carreaux_200m_mart.csv', nb;
END $$;


-- ═══════════════════════════════════════════════════════
-- FICHIER 3 : La Réunion
-- ═══════════════════════════════════════════════════════
TRUNCATE public.filosofi_load;

COPY public.filosofi_load (
    idcar_200m, idcar_1km, idcar_nat, i_est_200, i_est_1km, lcog_geo,
    ind, men, men_pauv, men_1ind, men_5ind, men_prop, men_fmp,
    ind_snv, men_surf, men_coll, men_mais,
    log_av45, log_45_70, log_70_90, log_ap90, log_inc, log_soc,
    ind_0_3, ind_4_5, ind_6_10, ind_11_17, ind_18_24,
    ind_25_39, ind_40_54, ind_55_64, ind_65_79, ind_80p, ind_inc
)
FROM '/work/PERSO/ASSO/data/INSEE/Filosofi2021_carreaux_200m_csv/carreaux_200m_reun.csv'
WITH (
    FORMAT CSV,
    DELIMITER ',',
    HEADER TRUE,
    ENCODING 'UTF8',
    FORCE_NOT_NULL (idcar_200m, idcar_1km, idcar_nat)
);

INSERT INTO public.filosofi_200m (
    annee,
    idcar_200m, i_est_200, idcar_1km, i_est_1km, idcar_nat,
    groupe,
    ind, men_1ind, men_5ind, men_prop, men_fmp,
    ind_snv, men_surf, men_coll, men_mais,
    log_av45, log_45_70, log_70_90, log_ap90, log_inc, log_soc,
    ind_0_3, ind_4_5, ind_6_10, ind_11_17, ind_18_24,
    ind_25_39, ind_40_54, ind_55_64, ind_65_79, ind_80p, ind_inc,
    men_pauv, men, lcog_geo
)
SELECT
    2021,
    idcar_200m, i_est_200, idcar_1km, i_est_1km, idcar_nat,
    groupe,
    ind, men_1ind, men_5ind, men_prop, men_fmp,
    ind_snv, men_surf, men_coll, men_mais,
    log_av45, log_45_70, log_70_90, log_ap90, log_inc, log_soc,
    ind_0_3, ind_4_5, ind_6_10, ind_11_17, ind_18_24,
    ind_25_39, ind_40_54, ind_55_64, ind_65_79, ind_80p, ind_inc,
    men_pauv, men, lcog_geo
FROM public.filosofi_load;

DO $$
DECLARE nb BIGINT;
BEGIN
    SELECT COUNT(*) INTO nb FROM public.filosofi_load;
    RAISE NOTICE '[REUN] % carreaux chargés depuis carreaux_200m_reun.csv', nb;
END $$;


-- -------------------------------------------------------
-- Vérification finale
-- -------------------------------------------------------
DO $$
DECLARE
    nb_total   BIGINT;
    nb_approx  BIGINT;
BEGIN
    SELECT COUNT(*) INTO nb_total  FROM public.filosofi_200m WHERE annee = 2021;
    SELECT COUNT(*) INTO nb_approx FROM public.filosofi_200m WHERE annee = 2021 AND i_est_200 = 1;
    RAISE NOTICE '=== Chargement 2021 terminé ===';
    RAISE NOTICE '  Total carreaux    : %', nb_total;
    RAISE NOTICE '  Dont approchés    : % (i_est_200=1)', nb_approx;
    RAISE NOTICE '  Attendu           : ~2 324 577';
END $$;

COMMIT;
