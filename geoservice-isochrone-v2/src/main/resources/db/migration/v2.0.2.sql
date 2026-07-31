-- Ajout ITERATIVE_DBSCAN aux contraintes CHECK des colonnes type_algo
-- Hibernate auto-génère ces contraintes depuis l'enum Java mais ne les met pas à jour
-- lors de l'ajout d'une nouvelle valeur d'enum.

ALTER TABLE IF EXISTS public.park_proposal_work
  DROP CONSTRAINT IF EXISTS park_proposal_work_type_algo_check;
ALTER TABLE IF EXISTS public.park_proposal_work
  ADD CONSTRAINT park_proposal_work_type_algo_check
  CHECK (type_algo IN (
    'ITERATIVE_1', 'ITERATIVE_2', 'ITERATIVE_3', 'ITERATIVE_DBSCAN',
    'PPC_1', 'PPC_2', 'PPC_3',
    'CHI2_5', 'CHI2_6',
    'GENETIC_7'
  ));

ALTER TABLE IF EXISTS public.park_proposal_meta
  DROP CONSTRAINT IF EXISTS park_proposal_meta_type_algo_check;
ALTER TABLE IF EXISTS public.park_proposal_meta
  ADD CONSTRAINT park_proposal_meta_type_algo_check
  CHECK (type_algo IN (
    'ITERATIVE_1', 'ITERATIVE_2', 'ITERATIVE_3', 'ITERATIVE_DBSCAN',
    'PPC_1', 'PPC_2', 'PPC_3',
    'CHI2_5', 'CHI2_6',
    'GENETIC_7'
  ));
