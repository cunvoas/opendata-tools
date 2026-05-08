SELECT json_build_object(
    id_inspire, 

    replace(
        replace(
            replace(ST_AsText(geo_point_2d), 'POINT(','[')
            , ')',']')
        , ' ',',')
    )
    FROM public.carre200onlyshape

    where id_inspire='CRS3035RES200mN3079800E3833800'
    limit 10;
    
--=============
select id_inspire,
	json_build_object(
	    'loc', json_build_array(  st_y(geo_point_2d) , st_x(geo_point_2d))
	)
FROM public.carre200onlyshape

where id_inspire='CRS3035RES200mN3079800E3833800'
    limit 10;
    
--    =========================
copy (
 select id_inspire,
    json_build_object(
        'loc', json_build_array(  st_y(geo_point_2d) , st_x(geo_point_2d))
    )
FROM public.carre200onlyshape
) to '/tmp/loc_inspire_full.csv' with delimiter ',' CSV header; 


    
-- CRS3035RES200mN3079800E3833800
-- /localization/inspire/CRS3035RES200m/N3079/E3833/CRS3035RES200mN3079800E3833800.json


--==========================
    copy (
    SELECT json_build_object(
    id_inspire, 

    replace(
        replace(
            replace(ST_AsText(geo_point_2d), 'POINT(','[')
            , ')',']')
        , ' ',',')
    )
    FROM public.carre200onlyshape

) to '/tmp/loc_inspire.csv' with delimiter ',' CSV header; 






--=============

SELECT cc.*
FROM public.carre200_computed_v2 cc 
WHERE cc.annee=2019  AND cc.id_inspire in ('CRS3035RES200mN3079400N3833400''CRS3035RES200mN3079400N3833600''CRS3035RES200mN3079400N3833800''CRS3035RES200mN3079400N3834000''CRS3035RES200mN3079400N3834200''CRS3035RES200mN3079600N3833400''CRS3035RES200mN3079600N3833600''CRS3035RES200mN3079600N3833800''CRS3035RES200mN3079600N3834000''CRS3035RES200mN3079600N3834200''CRS3035RES200mN3079800N3833400''CRS3035RES200mN3079800N3833600''CRS3035RES200mN3079800N3833800''CRS3035RES200mN3079800N3834000''CRS3035RES200mN3079800N3834200''CRS3035RES200mN3080000N3833400''CRS3035RES200mN3080000N3833600''CRS3035RES200mN3080000N3833800''CRS3035RES200mN3080000N3834000''CRS3035RES200mN3080000N3834200''CRS3035RES200mN3080200N3833400''CRS3035RES200mN3080200N3833600''CRS3035RES200mN3080200N3833800''CRS3035RES200mN3080200N3834000''CRS3035RES200mN3080200N3834200')


