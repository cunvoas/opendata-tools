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
-- Sécurité : S'assurer que la table de staging existe
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.filosofi_load (
    idcar_200m VARCHAR(30) not null,
    i_est_200 numeric(1),
    idcar_1km VARCHAR(31) not null,
    i_est_1km numeric(1),
    idcar_nat VARCHAR(36) not null,
    groupe numeric(16, 4),
    ind numeric(16, 4),
    men_1ind numeric(16, 4),
    men_5ind numeric(16, 4),
    men_prop numeric(16, 4),
    men_fmp numeric(16, 4),
    ind_snv numeric(16, 4),
    men_surf numeric(16, 4),
    men_coll numeric(16, 4),
    men_mais numeric(16, 4),
    log_av45 numeric(16, 4),
    log_45_70 numeric(16, 4),
    log_70_90 numeric(16, 4),
    log_ap90 numeric(16, 4),
    log_inc numeric(16, 4),
    log_soc numeric(16, 4),
    ind_0_3 numeric(16, 4),
    ind_4_5 numeric(16, 4),
    ind_6_10 numeric(16, 4),
    ind_11_17 numeric(16, 4),
    ind_18_24 numeric(16, 4),
    ind_25_39 numeric(16, 4),
    ind_40_54 numeric(16, 4),
    ind_55_64 numeric(16, 4),
    ind_65_79 numeric(16, 4),
    ind_80p numeric(16, 4),
    ind_inc numeric(16, 4),
    men_pauv numeric(16, 4),
    men numeric(16, 4),
    lcog_geo VARCHAR(50)
);

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



COMMIT;