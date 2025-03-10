
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
  

-- flowerbed
update  public.parc_jardin set type_id=4 where identifiant in (184);
-- concrete
update  public.parc_jardin set type_id=5 where identifiant in (24,96);

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


-- ajout du code insee dans les carreau
UPDATE public.carre200onlyshape
SET code_insee=c.id_insee
FROM   public.cadastre c
WHERE ST_intersects(public.carre200onlyshape.geo_shape, c.geo_shape)
    AND code_insee is null;
--UPDATE 13702395
--Query complete 10:32:23.749 (Raspberry PI 4)

    
-- si pas assez d'espace disque (15 Go env)
UPDATE public.carre200onlyshape SET code_insee=c.id_insee
    FROM   public.cadastre c
    WHERE ST_intersects(public.carre200onlyshape.geo_shape, c.geo_shape) 
    and code_insee is null and c.id_insee like '0%';
UPDATE public.carre200onlyshape SET code_insee=c.id_insee
    FROM   public.cadastre c
    WHERE ST_intersects(public.carre200onlyshape.geo_shape, c.geo_shape) 
    and code_insee is null and c.id_insee like '1%';
UPDATE public.carre200onlyshape SET code_insee=c.id_insee
    FROM   public.cadastre c
    WHERE ST_intersects(public.carre200onlyshape.geo_shape, c.geo_shape) 
    and code_insee is null and c.id_insee like '2%';
UPDATE public.carre200onlyshape SET code_insee=c.id_insee
    FROM   public.cadastre c
    WHERE ST_intersects(public.carre200onlyshape.geo_shape, c.geo_shape) 
    and code_insee is null and c.id_insee like '3%';
UPDATE public.carre200onlyshape SET code_insee=c.id_insee
    FROM   public.cadastre c
    WHERE ST_intersects(public.carre200onlyshape.geo_shape, c.geo_shape) 
    and code_insee is null and c.id_insee like '4%';
UPDATE public.carre200onlyshape SET code_insee=c.id_insee
    FROM   public.cadastre c
    WHERE ST_intersects(public.carre200onlyshape.geo_shape, c.geo_shape) 
    and code_insee is null and c.id_insee like '5%';
UPDATE public.carre200onlyshape SET code_insee=c.id_insee
    FROM   public.cadastre c
    WHERE ST_intersects(public.carre200onlyshape.geo_shape, c.geo_shape) 
    and code_insee is null and c.id_insee like '6%';
UPDATE public.carre200onlyshape SET code_insee=c.id_insee
    FROM   public.cadastre c
    WHERE ST_intersects(public.carre200onlyshape.geo_shape, c.geo_shape) 
    and code_insee is null and c.id_insee like '7%';
UPDATE public.carre200onlyshape SET code_insee=c.id_insee
    FROM   public.cadastre c
    WHERE ST_intersects(public.carre200onlyshape.geo_shape, c.geo_shape) 
    and code_insee is null and c.id_insee like '8%';
UPDATE public.carre200onlyshape SET code_insee=c.id_insee
    FROM   public.cadastre c
    WHERE ST_intersects(public.carre200onlyshape.geo_shape, c.geo_shape) 
    and code_insee is null and c.id_insee like '9%';


    