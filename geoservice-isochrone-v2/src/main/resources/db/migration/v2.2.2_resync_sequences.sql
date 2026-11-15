-- Resynchronise les séquences Hibernate avec les données existantes.
-- Cause : des lignes insérées avec un id explicite (restauration, import) ne font pas
-- avancer la séquence -> "duplicate key value violates unique constraint ..._pkey".
-- Sans effet si la séquence est déjà à jour.

DO $$
DECLARE
    r record;
BEGIN
    FOR r IN SELECT * FROM (VALUES
        ('seq_activity_stats', 'adm_activity_stats', 'id'),
        ('seq_asso',           'adm_asso',           'id'),
        ('seq_city',           'city',               'id'),
        ('seq_com2com',        'adm_com2commune',    'id'),
        ('seq_contrib',        'adm_contrib',        'id'),
        ('seq_dashboard',      'dashboard',          'id'),
        ('seq_mel_park',       'parc_jardin',        'identifiant'),
        ('seq_park',           'park_area',          'id'),
        ('seq_park_entrance',  'park_entrance',      'id'),
        ('seq_park_pref',      'parc_prefecture',    'identifiant'),
        ('seq_region',         'adm_region',         'id'),
        ('seq_photo',          'parc_photo',         'id')
    ) AS t(seq, tbl, col)
    LOOP
        IF to_regclass('public.' || r.tbl) IS NOT NULL
           AND to_regclass('public.' || r.seq) IS NOT NULL THEN
            EXECUTE format(
                'SELECT setval(%L, COALESCE((SELECT MAX(%I) FROM public.%I), 0) + 1, false)',
                'public.' || r.seq, r.col, r.tbl);
        END IF;
    END LOOP;
END $$;
