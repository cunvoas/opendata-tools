WITH entr as (
    select min(id) as id, area_id 
    from park_entrance
    group by area_id
)

SELECT identifiant, st_astext(coordonnee), nom_parc, surface, type, id_city, source, status, pe.entrance_point
    FROM public.parc_jardin pj
    inner join park_area pa on pj.identifiant = pa.id_parc_jardin
    inner join park_entrance pe on pe.area_id = pa.id
    inner join entr e on e.id=pe.id
where pj.status=2 
    and pj.coordonnee isnull
    --and pe.description='e1'
    --and id_city isnullteo
    
    order by pj.identifiant;