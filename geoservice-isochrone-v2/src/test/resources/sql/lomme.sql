SELECT pj.identifiant, pj.nom_parc,
    --, quartier, sous_type, surface, type, id_city, 
    pj.source,
    pe.id,pe.description, ST_AsText(pe.entrance_point)

    FROM public.parc_jardin pj
    INNER JOIN public.park_area pa on pa.id_parc_jardin=pj.identifiant
    INNER JOIN public.park_entrance pe on pa.id=pe.area_id 
    
    WHERE 
--  identifiant in (130)    OR 
--  adresse like '%Lamy%'

    nom_parc like '%Voltaire%'