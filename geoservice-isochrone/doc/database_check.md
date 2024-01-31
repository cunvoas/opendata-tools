 Contrôle des lignes en base:
 
	select count(1) as lig, 'adm_activity_stats' as tbl from adm_activity_stats union 
	select count(1) as lig, 'adm_asso' as tbl from adm_asso union 
	select count(1) as lig, 'adm_com2commune' as tbl from adm_com2commune union 
	select count(1) as lig, 'adm_contrib' as tbl from adm_contrib union 
	select count(1) as lig, 'adm_contrib_action' as tbl from adm_contrib_action union 
	select count(1) as lig, 'adm_region' as tbl from adm_region union 
	select count(1) as lig, 'cadastre' as tbl from cadastre union 
	select count(1) as lig, 'carre200' as tbl from carre200 union 
	select count(1) as lig, 'carre200_computed' as tbl from carre200_computed union 
	select count(1) as lig, 'carre200shape' as tbl from carre200shape union 
	select count(1) as lig, 'city' as tbl from city union 
	select count(1) as lig, 'dashboard' as tbl from dashboard union 
	select count(1) as lig, 'filosofi_200_2015' as tbl from filosofi_200_2015 union 
	select count(1) as lig, 'insee_densite_city' as tbl from insee_densite_city union 
	select count(1) as lig, 'laposte' as tbl from laposte union 
	select count(1) as lig, 'parc_jardin' as tbl from parc_jardin union 
	select count(1) as lig, 'parc_prefecture' as tbl from parc_prefecture union 
	select count(1) as lig, 'park_area' as tbl from park_area union 
	select count(1) as lig, 'park_area_computed' as tbl from park_area_computed union 
	select count(1) as lig, 'park_entrance' as tbl from park_entrance 
	order by tbl
	; 


Resync sequences after restore :

    SELECT setval('public.seq_activity_stats', (select max(id)+10 from adm_activity_stats) , true);
    SELECT setval('public.seq_asso', (select max(id)+10 from adm_asso) , true);
    SELECT setval('public.seq_city', (select max(id)+10 from city) , true);
    SELECT setval('public.seq_com2com', (select max(id)+10 from adm_com2commune) , true);
    SELECT setval('public.seq_contrib', (select max(id)+10 from adm_contrib) , true);
    SELECT setval('public.seq_dashboard', (select max(id)+10 from dashboard) , true);
    SELECT setval('public.seq_mel_park', (select max(identifiant)+10 from parc_jardin) , true);
    SELECT setval('public.seq_park', (select max(id)+10 from park_area) , true);
    SELECT setval('public.seq_park_entrance', (select max(id)+10 from park_entrance) , true);
    SELECT setval('public.seq_park_pref', (select max(identifiant)+10 from parc_prefecture) , true);
    
    ALTER SEQUENCE IF EXISTS public.seq_activity_stats INCREMENT 50;
    ALTER SEQUENCE IF EXISTS public.seq_asso INCREMENT 50;
    ALTER SEQUENCE IF EXISTS public.seq_city INCREMENT 50;
    ALTER SEQUENCE IF EXISTS public.seq_com2com INCREMENT 50;
    ALTER SEQUENCE IF EXISTS public.seq_contrib INCREMENT 50;
    ALTER SEQUENCE IF EXISTS public.seq_dashboard INCREMENT 50;
    ALTER SEQUENCE IF EXISTS public.seq_mel_park INCREMENT 50;
    ALTER SEQUENCE IF EXISTS public.seq_park INCREMENT 50;
    ALTER SEQUENCE IF EXISTS public.seq_mel_park INCREMENT 50;
    ALTER SEQUENCE IF EXISTS public.seq_park_pref INCREMENT 50;




