-- Nettoyage fiable d'un doublon de parc, sous forme de fonction stockée.
--
-- Principes :
--   * un seul des deux paramètres p_park_area_id / p_parc_jardin_id doit être fourni ;
--   * p_dry_run = true par défaut : AUCUNE suppression, seulement les compteurs ;
--   * tout est exécuté dans la transaction de l'appelant : COMMIT ou ROLLBACK au choix ;
--   * verrous SHARE ROW EXCLUSIVE pour bloquer le batch NDVI/calcul concurrent ;
--   * retourne le nombre de lignes supprimées (ou à supprimer en dry-run) par étape.
--
-- Usage :
--   -- simulation
--   SELECT * FROM public.clean_park_duplicate(p_park_area_id => 346);
--
--   -- suppression réelle, avec possibilité d'annuler
--   BEGIN;
--   SELECT * FROM public.clean_park_duplicate(p_park_area_id => 346, p_dry_run => false);
--   -- vérifier, puis COMMIT; ou ROLLBACK;
--
--   -- suppression d'un parc et de toutes ses géométries
--   SELECT * FROM public.clean_park_duplicate(p_parc_jardin_id => 129, p_dry_run => false);

CREATE OR REPLACE FUNCTION public.clean_park_duplicate(
    p_park_area_id  bigint  DEFAULT NULL,
    p_parc_jardin_id bigint DEFAULT NULL,
    p_dry_run       boolean DEFAULT true
)
RETURNS TABLE(step text, deleted bigint)
LANGUAGE plpgsql
VOLATILE
SECURITY INVOKER
SET search_path = pg_catalog, public
AS $$
DECLARE
    v_area_ids bigint[];
    v_n        bigint;
BEGIN
    IF (p_park_area_id IS NULL) = (p_parc_jardin_id IS NULL) THEN
        RAISE EXCEPTION
            'Fournir exactement un des paramètres p_park_area_id ou p_parc_jardin_id'
            USING ERRCODE = '22023';
    END IF;

    SELECT array_agg(a.id) INTO v_area_ids
    FROM public.park_area a
    WHERE (p_park_area_id  IS NOT NULL AND a.id = p_park_area_id)
       OR (p_parc_jardin_id IS NOT NULL AND a.id_parc_jardin = p_parc_jardin_id);

    IF v_area_ids IS NULL THEN
        RAISE NOTICE 'Aucune cible pour park_area_id=% / parc_jardin_id=%',
            p_park_area_id, p_parc_jardin_id;
        step := 'aucune_cible'; deleted := 0; RETURN NEXT;
        RETURN;
    END IF;

    -- Bloque les écritures concurrentes (batch NDVI / calcul) sur les tables touchées.
    LOCK TABLE public.park_area, public.park_area_computed, public.ndvi_job,
               public.ndvi_observation, public.ndvi_result, public.park_entrance,
               public.parc_prefecture, public.parc_photo
          IN SHARE ROW EXCLUSIVE MODE;

    IF p_dry_run THEN
        SELECT count(*) INTO v_n FROM public.ndvi_result r
         WHERE r.park_area_computed_id = ANY(v_area_ids)
            OR (p_parc_jardin_id IS NOT NULL AND r.parc_et_jardin_id = p_parc_jardin_id);
        step := 'ndvi_result'; deleted := v_n; RETURN NEXT;

        SELECT count(*) INTO v_n FROM public.ndvi_observation o
         WHERE o.park_area_id = ANY(v_area_ids);
        step := 'ndvi_observation'; deleted := v_n; RETURN NEXT;

        SELECT count(*) INTO v_n FROM public.ndvi_job j
         WHERE j.id = ANY(v_area_ids);
        step := 'ndvi_job'; deleted := v_n; RETURN NEXT;

        SELECT count(*) INTO v_n FROM public.park_area_computed c
         WHERE c.id = ANY(v_area_ids);
        step := 'park_area_computed'; deleted := v_n; RETURN NEXT;

        SELECT count(*) INTO v_n FROM public.park_entrance e
         WHERE e.area_id = ANY(v_area_ids);
        step := 'park_entrance'; deleted := v_n; RETURN NEXT;

        SELECT count(*) INTO v_n FROM public.park_area a
         WHERE a.id = ANY(v_area_ids);
        step := 'park_area'; deleted := v_n; RETURN NEXT;

        IF p_parc_jardin_id IS NOT NULL THEN
            SELECT count(*) INTO v_n FROM public.parc_prefecture p
             WHERE p.id_parc = p_parc_jardin_id;
            step := 'parc_prefecture'; deleted := v_n; RETURN NEXT;

            SELECT count(*) INTO v_n FROM public.parc_photo p
             WHERE p.parc_id = p_parc_jardin_id;
            step := 'parc_photo'; deleted := v_n; RETURN NEXT;

            SELECT count(*) INTO v_n FROM public.parc_jardin p
             WHERE p.identifiant = p_parc_jardin_id;
            step := 'parc_jardin'; deleted := v_n; RETURN NEXT;
        END IF;
        RETURN;
    END IF;

    -- Suppression réelle : enfants d'abord.
    DELETE FROM public.ndvi_result r
     WHERE r.park_area_computed_id = ANY(v_area_ids)
        OR (p_parc_jardin_id IS NOT NULL AND r.parc_et_jardin_id = p_parc_jardin_id);
    GET DIAGNOSTICS v_n = ROW_COUNT;
    step := 'ndvi_result'; deleted := v_n; RETURN NEXT;

    DELETE FROM public.ndvi_observation o WHERE o.park_area_id = ANY(v_area_ids);
    GET DIAGNOSTICS v_n = ROW_COUNT;
    step := 'ndvi_observation'; deleted := v_n; RETURN NEXT;

    DELETE FROM public.ndvi_job j WHERE j.id = ANY(v_area_ids);
    GET DIAGNOSTICS v_n = ROW_COUNT;
    step := 'ndvi_job'; deleted := v_n; RETURN NEXT;

    DELETE FROM public.park_area_computed c WHERE c.id = ANY(v_area_ids);
    GET DIAGNOSTICS v_n = ROW_COUNT;
    step := 'park_area_computed'; deleted := v_n; RETURN NEXT;

    DELETE FROM public.park_entrance e WHERE e.area_id = ANY(v_area_ids);
    GET DIAGNOSTICS v_n = ROW_COUNT;
    step := 'park_entrance'; deleted := v_n; RETURN NEXT;

    DELETE FROM public.park_area a WHERE a.id = ANY(v_area_ids);
    GET DIAGNOSTICS v_n = ROW_COUNT;
    step := 'park_area'; deleted := v_n; RETURN NEXT;

    IF p_parc_jardin_id IS NOT NULL THEN
        DELETE FROM public.parc_prefecture p WHERE p.id_parc = p_parc_jardin_id;
        GET DIAGNOSTICS v_n = ROW_COUNT;
        step := 'parc_prefecture'; deleted := v_n; RETURN NEXT;

        DELETE FROM public.parc_photo p WHERE p.parc_id = p_parc_jardin_id;
        GET DIAGNOSTICS v_n = ROW_COUNT;
        step := 'parc_photo'; deleted := v_n; RETURN NEXT;

        DELETE FROM public.parc_jardin p WHERE p.identifiant = p_parc_jardin_id;
        GET DIAGNOSTICS v_n = ROW_COUNT;
        step := 'parc_jardin'; deleted := v_n; RETURN NEXT;
    END IF;
END;
$$;

COMMENT ON FUNCTION public.clean_park_duplicate(bigint, bigint, boolean) IS
    'Supprime un doublon park_area ou parc_jardin et ses dépendances. p_dry_run=true par défaut (aucune écriture).';
