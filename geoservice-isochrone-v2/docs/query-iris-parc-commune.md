# Requête SQL – Couverture parcs par IRIS pour une commune INSEE

> **Commune cible :** `59350` (Roubaix)
> **Base de données :** PostgreSQL + PostGIS
> **Objectif :** lister tous les carrés 200m calculés, les agréger par IRIS et produire les totaux de population couverte et de surface de parc accessible.

---

## Tables impliquées

| Table JPA | Entité Java | Rôle |
|---|---|---|
| `carre200onlyshape` | `InseeCarre200mOnlyShape` | Géométrie et commune de chaque carreau 200m |
| `carre200_computed_v2` | `InseeCarre200mComputedV2` | Résultats de calcul d'accessibilité par carreau |
| `iris_shape` | `IrisShape` | Contour géométrique de chaque IRIS (jointure spatiale) |
| `iris_data` | `IrisData` | Données démographiques INSEE par IRIS (contrôle optionnel) |

---

## Requête SQL

```sql
-- =============================================================================
-- Couverture parcs par IRIS – commune INSEE 59350
-- =============================================================================
--
-- Pourquoi des CTE (WITH) ?
--   • Lisibilité : chaque bloc nomme et documente une étape logique.
--   • Maintenabilité : on modifie un seul bloc sans toucher au reste.
--   • Performance : PostgreSQL matérialise les CTE marquées MATERIALIZED,
--     ce qui évite de recalculer le même sous-ensemble plusieurs fois.
--   • Débogage : on peut tester chaque CTE isolément.
--
-- Structure :
--   1. carres_commune     → carrés 200m habités de la commune
--   2. iris_commune       → IRIS de la commune avec leur contour
--   3. carres_par_iris    → affectation spatiale carré → IRIS
--   4. carres_avec_calcul → enrichissement avec les données calculées
--   5. SELECT final       → agrégation par IRIS / année
-- =============================================================================

WITH

    -- -------------------------------------------------------------------------
    -- CTE 1 : carres_commune
    -- Sélectionne les carrés de 200m appartenant à la commune cible.
    -- Seuls les carrés avec population (avec_pop = TRUE) sont retenus
    -- pour ne pas gonfler les comptages avec des zones vides.
    -- Source : carre200onlyshape  (InseeCarre200mOnlyShape)
    -- -------------------------------------------------------------------------
    carres_commune AS MATERIALIZED (
        SELECT
            cs.id_inspire,
            cs.geo_point_2d    -- centroïde du carreau, utilisé pour la jointure spatiale
        FROM public.carre200onlyshape cs
        WHERE cs.code_insee = '59350'
          AND cs.avec_pop    = TRUE
    ),

    -- -------------------------------------------------------------------------
    -- CTE 2 : iris_commune
    -- Récupère les IRIS de la même commune avec leur contour géométrique.
    -- Le champ "contour" (Geometry) de iris_shape sert de zone de référence
    -- pour la jointure spatiale qui suit.
    -- Source : iris_shape  (IrisShape)
    -- -------------------------------------------------------------------------
    iris_commune AS MATERIALIZED (
        SELECT
            ish.code_iris,
            ish.nom_iris,
            ish.type_iris,
            ish.contour
        FROM public.iris_shape ish
        WHERE ish.code_insee = '59350'
    ),

    -- -------------------------------------------------------------------------
    -- CTE 3 : carres_par_iris
    -- Jointure spatiale : affecte chaque carreau à l'IRIS dont le contour
    -- contient son centroïde (geo_point_2d).
    -- ST_Within garantit qu'un carreau n'est compté que dans UN seul IRIS,
    -- sans double-comptage aux frontières.
    -- -------------------------------------------------------------------------
    carres_par_iris AS (
        SELECT
            ic.code_iris,
            ic.nom_iris,
            ic.type_iris,
            cc.id_inspire
        FROM carres_commune cc
        JOIN iris_commune   ic
          ON ST_Within(cc.geo_point_2d, ic.contour)
    ),

    -- -------------------------------------------------------------------------
    -- CTE 4 : carres_avec_calcul
    -- Enrichit chaque carreau avec ses indicateurs d'accessibilité calculés.
    -- Source : carre200_computed_v2  (InseeCarre200mComputedV2)
    --
    -- Colonnes clés :
    --   pop_all              → population totale du carreau
    --   pop_inc              → population ayant accès à un parc (prorata surface)
    --   pop_exc              → population sans accès à un parc
    --   surface_access_park  → surface cumulée des parcs accessibles (m²)
    --   surface_park_pcapita → surface de parc par habitant (m²/hab)
    --   pop_inc_oms          → population avec accès à un parc conforme OMS
    --                          (seuil : 10 m²/hab)
    --   surface_access_park_oms → surface des parcs conformes OMS (m²)
    --   sustainable_park_is  → vrai si le carreau a un parc durable
    --                          (≥ 5 000 m² dans un rayon de 300 m)
    --   sustainable_park_pop → population couverte par un parc durable
    -- -------------------------------------------------------------------------
    carres_avec_calcul AS (
        SELECT
            cpi.code_iris,
            cpi.nom_iris,
            cpi.type_iris,
            cv2.annee,
            cv2.id_inspire,
            cv2.pop_all,
            cv2.pop_inc,
            cv2.pop_exc,
            cv2.surface_access_park,
            cv2.surface_park_pcapita,
            cv2.pop_inc_oms,
            cv2.surface_access_park_oms,
            cv2.sustainable_park_is,
            cv2.sustainable_park_pop
        FROM carres_par_iris        cpi
        JOIN public.carre200_computed_v2 cv2
          ON cv2.id_inspire = cpi.id_inspire
    )

-- =============================================================================
-- Résultat final : agrégation par IRIS et par année
--
-- Indicateurs produits :
--   nb_carres              nombre de carrés 200m dans l'IRIS
--   pop_totale             Σ population totale
--   pop_avec_parc          Σ population ayant accès à un parc
--   pop_sans_parc          Σ population sans accès à un parc
--   taux_couverture_pct    taux de couverture (%)
--   surface_parc_m2        Σ surface de parcs accessible (m²)
--   surface_moy_pcapita    surface moyenne par habitant parmi les couverts (m²/hab)
--   pop_avec_parc_oms      Σ population avec accès à un parc conforme OMS
--   surface_parc_oms_m2    Σ surface de parcs OMS accessible (m²)
--   pop_avec_parc_durable  Σ population couverte par un parc durable
-- =============================================================================
SELECT
    cac.annee,
    cac.code_iris,
    cac.nom_iris,
    cac.type_iris,

    COUNT(cac.id_inspire)                                        AS nb_carres,

    -- Population générale
    ROUND(SUM(cac.pop_all),  2)                                  AS pop_totale,
    ROUND(SUM(cac.pop_inc),  2)                                  AS pop_avec_parc,
    ROUND(SUM(cac.pop_exc),  2)                                  AS pop_sans_parc,

    -- Taux de couverture : NULLIF évite la division par zéro quand pop_all = 0
    ROUND(
        SUM(cac.pop_inc) * 100.0
        / NULLIF(SUM(cac.pop_all), 0),
    1)                                                           AS taux_couverture_pct,

    -- Surface de parcs accessible
    ROUND(SUM(cac.surface_access_park), 2)                       AS surface_parc_m2,

    -- Surface moyenne par habitant parmi les personnes couvertes
    ROUND(
        SUM(cac.surface_access_park)
        / NULLIF(SUM(cac.pop_inc), 0),
    2)                                                           AS surface_moy_pcapita,

    -- Indicateurs OMS (seuil 10 m²/hab)
    ROUND(SUM(cac.pop_inc_oms),            2)                    AS pop_avec_parc_oms,
    ROUND(SUM(cac.surface_access_park_oms), 2)                   AS surface_parc_oms_m2,

    -- Parc durable (≥ 5 000 m² dans un rayon de 300 m)
    ROUND(
        SUM(
            CASE WHEN cac.sustainable_park_is = TRUE
                 THEN cac.sustainable_park_pop
                 ELSE 0
            END
        ),
    2)                                                           AS pop_avec_parc_durable

FROM carres_avec_calcul cac
GROUP BY
    cac.annee,
    cac.code_iris,
    cac.nom_iris,
    cac.type_iris
ORDER BY
    cac.annee    DESC,
    cac.code_iris ASC
;
```

---

## Explication des jointures

```
carre200onlyshape (code_insee = '59350')
        │
        │ id_inspire  ←──────────────────────────────────┐
        │                                                  │
        ▼ ST_Within(geo_point_2d, contour)                │
iris_shape (code_insee = '59350')                         │
        │                                                  │
        │ code_iris (agrégation finale)                   │
        │                                                  │
        └────────────────── JOIN ─────────────────────────┘
                                                           │
                                             carre200_computed_v2
                                             (annee, id_inspire)
```

---

## Variante : filtrer sur une année précise

Ajouter un filtre dans la **CTE 4** ou en condition finale :

```sql
-- dans carres_avec_calcul, modifier le JOIN :
JOIN public.carre200_computed_v2 cv2
  ON  cv2.id_inspire = cpi.id_inspire
  AND cv2.annee      = 2021          -- ← année souhaitée

-- ou en WHERE sur la requête finale :
WHERE cac.annee = 2021
```

---

## Variante : inclure les données démographiques INSEE

Pour croiser avec `iris_data` (`IrisData`) et obtenir la population officielle INSEE par IRIS :

```sql
-- ajouter une CTE supplémentaire après carres_avec_calcul :
iris_pop AS (
    SELECT id.iris, id.annee, id.pop AS pop_insee
    FROM public.iris_data id
    WHERE id.com = '59350'
),

-- puis joindre dans la requête finale :
LEFT JOIN iris_pop ip
       ON ip.iris  = cac.code_iris
      AND ip.annee = cac.annee
```

---

## Notes techniques

| Point | Détail |
|---|---|
| **PostGIS requis** | `ST_Within` est une fonction PostGIS. La base doit avoir l'extension activée. |
| **SRID** | `geo_point_2d` et `contour` doivent être dans le même SRID (4326). Sinon utiliser `ST_Transform`. |
| **Carreaux aux limites d'IRIS** | `ST_Within` peut exclure un centroïde exactement sur une frontière. Utiliser `ST_Intersects` si des carrés sont perdus. |
| **Performance** | Les index spatiaux sur `geo_point_2d` (table `carre200onlyshape`) et `contour` (table `iris_shape`) sont indispensables pour cette jointure. |
| **Double-comptage OMS** | `surface_access_park_oms` peut chevaucher `surface_access_park`. Ne pas additionner les deux pour obtenir une surface totale. |
