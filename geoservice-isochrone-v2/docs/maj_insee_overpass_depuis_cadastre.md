# Mise à jour du code INSEE Overpass via Cadastre

Met à jour la colonne `insee` de `park_overpass` en recoupement spatial avec `cadastre.geo_shape`.

## Structure des tables

| Table | Colonne clé | Géométrie |
|-------|-----------|-----------|
| `park_overpass` | `insee` (varchar 5) | `shape` (geometry) |
| `cadastre` | `id_insee` (PK) | `geo_shape` (MultiPolygon 4326) |

## 1. Preview — voir les changements avant UPDATE

```sql
SELECT po.id,
       po.name,
       po.insee                                   AS insee_actuel,
       c.id_insee                                 AS insee_cadastre,
       c.nom                                      AS commune_cadastre,
       round(ST_Distance(ST_Centroid(po.shape), c.geo_shape)::numeric, 2) AS distance_m
FROM park_overpass po
JOIN cadastre c ON ST_Contains(c.geo_shape, ST_Centroid(po.shape))
WHERE po.insee IS NULL
   OR po.insee != c.id_insee
ORDER BY po.id;
```

## 2. UPDATE — corrige l'INSEE par intersection spatiale

```sql
UPDATE park_overpass po
SET insee = sub.id_insee
FROM (
    SELECT DISTINCT ON (po.id)
        po.id,
        c.id_insee
    FROM park_overpass po
    JOIN cadastre c ON ST_Contains(c.geo_shape, ST_Centroid(po.shape))
    WHERE po.shape IS NOT NULL
      AND (po.insee IS NULL OR po.insee != c.id_insee)
    ORDER BY po.id, ST_Area(c.geo_shape) ASC
) sub
WHERE po.id = sub.id;
```

## 3. Stats — comptage avant/après

```sql
-- État actuel
SELECT 'sans_insee' AS statut, count(*) AS nb FROM park_overpass WHERE insee IS NULL
UNION ALL
SELECT 'avec_insee', count(*) FROM park_overpass WHERE insee IS NOT NULL
UNION ALL
SELECT 'rattachable', count(DISTINCT po.id)
FROM park_overpass po
JOIN cadastre c ON ST_Contains(c.geo_shape, ST_Centroid(po.shape))
WHERE po.insee IS NULL OR po.insee != c.id_insee;
```

## 4. Cas particuliers — Overpass sans shape (nodes)

Pour les éléments OSM de type `node` où `shape` est NULL, tentative via `corner_south_west` :

```sql
SELECT po.id, po.name, po.insee, c.id_insee, c.nom
FROM park_overpass po
JOIN cadastre c ON ST_Contains(c.geo_shape, po.corner_south_west)
WHERE po.shape IS NULL
  AND (po.insee IS NULL OR po.insee != c.id_insee);
```

## 5. Rollback si nécessaire

```sql
-- Sauvegarde des valeurs avant UPDATE
CREATE TABLE park_overpass_insee_bkp AS
SELECT id, insee FROM park_overpass;

-- Restauration
UPDATE park_overpass po
SET insee = bkp.insee
FROM park_overpass_insee_bkp bkp
WHERE po.id = bkp.id;
```

## Notes

- `DISTINCT ON (po.id)` + `ORDER BY ST_Area ASC` : si un shape chevauche plusieurs communes, on prend la plus petite (la plus précise).
- Exécuter la **requête 1 (Preview)** avant la **requête 2 (UPDATE)**.
