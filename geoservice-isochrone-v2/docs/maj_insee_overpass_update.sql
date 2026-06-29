-- UPDATE basé sur ST_Intersects avec geo_shape_low (simplifié)
UPDATE public.park_overpass po
SET insee = c.id_insee
FROM public.cadastre c
WHERE ST_Intersects(po.shape, c.geo_shape_low)
  AND (po.insee IS NULL OR po.insee != c.id_insee);

-- Variante avec géométrie complète (plus précise, plus lente)
-- UPDATE public.park_overpass po
-- SET insee = c.id_insee
-- FROM public.cadastre c
-- WHERE ST_Intersects(po.shape, c.geo_shape)
--   AND (po.insee IS NULL OR po.insee != c.id_insee);

-- Preview avant exécution
SELECT po.id, po.name, po.insee AS insee_avant, c.id_insee AS insee_apres, c.nom AS commune
FROM public.park_overpass po
JOIN public.cadastre c ON ST_Intersects(po.shape, c.geo_shape_low)
WHERE po.insee IS NULL OR po.insee != c.id_insee
ORDER BY po.id
LIMIT 20;