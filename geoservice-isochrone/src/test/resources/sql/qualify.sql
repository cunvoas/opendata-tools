
-- non ouvert au public
-- lomme
 update public.park_area 
    SET  oms_custom=false 
    where id in (452,453,454,459,460,252,254,255,260,152);

-- hellemmes
 update public.park_area 
    SET  oms_custom=false 
    where id in (203);