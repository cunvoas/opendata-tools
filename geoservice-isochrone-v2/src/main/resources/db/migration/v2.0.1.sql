-- Geometry optimisation
ALTER TABLE IF EXISTS public.cadastre
  ADD COLUMN geo_shape_low geometry;
UPDATE public.cadastre
  SET geo_shape_low=ST_MakeValid(ST_Simplify(geo_shape, 0.0001));

  
ALTER TABLE IF EXISTS public.adm_com2commune
  ADD COLUMN geo_shape_low geometry; 
  
-- Mise à jour simple de geo_shape_low pour les communautés de communes basées sur l'union des cadastres de leurs villes
UPDATE adm_com2commune acc
SET carre_carte=sub.union_geom, geo_shape_low = ST_MakeValid(ST_Simplify(sub.union_geom, 0.0001))
FROM (
    SELECT 
        ci.id_comm2co, 
        ST_Multi(ST_Union(ca.geo_shape)) as union_geom
    FROM city ci
    JOIN cadastre ca ON ci.insee_code = ca.id_insee
    WHERE ci.id_comm2co IS NOT NULL
    GROUP BY ci.id_comm2co
) AS sub
WHERE acc.id = sub.id_comm2co;


-- Mise à jour optimisée de geo_shape_low pour les communautés de communes basées sur l'union des cadastres de leurs villes
UPDATE adm_com2commune acc
SET carre_carte = sub.union_geom,
    geo_shape_low = sub.final_geom
FROM (
    WITH raw_unions AS (
        SELECT 
            ci.id_comm2co, 
            ST_Multi(ST_Union(ca.geo_shape)) as union_geom
        FROM city ci
        JOIN cadastre ca ON ci.insee_code = ca.id_insee
        WHERE ci.id_comm2co IS NOT NULL
        GROUP BY ci.id_comm2co
    ),
    filled_rings AS (
        -- On remplit les trous (rings) dont l'aire est faible
        SELECT 
            id_comm2co,
            union_geom,
            ST_Collect(
                ARRAY(
                    SELECT ST_MakePolygon(
                        ST_ExteriorRing(poly.geom),
                        ARRAY(
                            SELECT ST_InteriorRingN(poly.geom, n)
                            FROM generate_series(1, ST_NumInteriorRings(poly.geom)) AS n
                            WHERE ST_Area(ST_MakePolygon(ST_InteriorRingN(poly.geom, n))) >= 0.0001
                        )
                    )
                    FROM ST_Dump(union_geom) AS poly
                )
            ) AS geometry_filled
        FROM raw_unions
    ),
    filtered_polygons AS (
        -- On filtre les petits polygones constituant le multipolygone
        SELECT 
            id_comm2co,
            union_geom,
            ST_Multi(ST_Union(dumped.geom)) as final_geom
        FROM (
            SELECT id_comm2co, union_geom, (ST_Dump(geometry_filled)).geom as geom
            FROM filled_rings
        ) dumped
        WHERE ST_Area(dumped.geom) >= 0.0001
        GROUP BY id_comm2co, union_geom
    )
    SELECT id_comm2co, union_geom, ST_MakeValid(ST_Simplify(final_geom, 0.0001)) as final_geom
    FROM filtered_polygons
) AS sub
WHERE acc.id = sub.id_comm2co;
