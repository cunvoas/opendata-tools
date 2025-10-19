WITH quartier as (
SELECT nom, contour
    FROM public.quartier
),
lille as (
SELECT code_iris, code_insee, nom_commune, iris, nom_iris, contour
    FROM public.iris_shape
    WHERE code_insee='59350'
)


SELECT distinct q.nom, v.code_iris, v.nom_iris
FROM quartier q   , lille as v
WHERE st_intersects(q.contour, v.contour)
order by q.nom,  v.nom_iris

 
limit 300