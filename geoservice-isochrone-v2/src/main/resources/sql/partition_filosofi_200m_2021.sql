-- ============================================================
-- Création de la partition 2021 de la table filosofi_200m
--
-- Prérequis :
--   La table parente filosofi_200m doit exister et être
--   partitionnée par LIST (annee) — voir filosofil.sql
--
-- Partitions existantes : 2015, 2017, 2019
-- Nouvelle partition     : 2021
--
-- Usage :
--   psql -U <user> -d <dbname> -f partition_filosofi_200m_2021.sql
-- ============================================================

-- Sécurité : ne crée la partition que si elle n'existe pas encore
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = 'filosofi_200m_2021'
          AND n.nspname = current_schema()
    ) THEN
        EXECUTE 'CREATE TABLE filosofi_200m_2021
                 PARTITION OF filosofi_200m
                 FOR VALUES IN (2021)';
        RAISE NOTICE 'Partition filosofi_200m_2021 créée.';
    ELSE
        RAISE NOTICE 'Partition filosofi_200m_2021 déjà existante — aucune action.';
    END IF;
END $$;
