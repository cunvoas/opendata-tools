INSERT INTO public.park_type(id, i18n, oms) VALUES (1, 'park.type.garden', true);
INSERT INTO public.park_type(id, i18n, oms) VALUES (2, 'park.type.street', false);
INSERT INTO public.park_type(id, i18n, oms) VALUES (3, 'park.type.smallest', false);
INSERT INTO public.park_type(id, i18n, oms) VALUES (4, 'park.type.flowerbed', false);
INSERT INTO public.park_type(id, i18n, oms) VALUES (5, 'park.type.concretePlace', false);
INSERT INTO public.park_type(id, i18n, oms) VALUES (6, 'park.type.graveyard', false);

UPDATE public.park_area SET type_id=1 ;
UPDATE public.park_area SET type_id=6 WHERE id_parc_jardin in (
    SELECT identifiant FROM public.parc_jardin WHERE type='Cimetière' );
UPDATE public.park_area SET type_id=4 WHERE id_parc_jardin in (
    SELECT identifiant FROM public.parc_jardin WHERE sous_type='Jardin de poche' );

commit;
