UPDATE public.park_overpass
SET insee = c.id_insee
FROM public.cadastre c
WHERE ST_Intersects(shape, c.geo_shape);