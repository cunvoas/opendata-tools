
-- non ouvert au public
-- lomme
 update public.park_area 
    SET  oms_custom=false 
    where id in (452,453,454,459,460,252,254,255,260,152);

-- hellemmes
 update public.park_area 
    SET  oms_custom=false 
    where id in (202, 203, 206);
   
    
-- places, pelouse non exploitable, et terrain fermés
 update public.park_area 
    SET  oms_custom=false 
    where id_parc_jardin in (15,40,88,44,34, 87,127, 123,68,37);
    --127 parc auto
    
/*
SELECT identifiant, adresse, nom_parc, quartier, sous_type, surface, type, id_city, source, status
    FROM public.parc_jardin where 
    --sous_type not in ('Jardin de poche', 'Cimetière')
    sous_type in ('Place','Pelouse')
    and aire_jeux='Non'
    order by quartier, hierarchie, identifiant

    
 */
UPDATE public.parc_jardin
SET surface_contour=ST_Area(contour, true)
WHERE contour is not null and surface_contour is null;
    