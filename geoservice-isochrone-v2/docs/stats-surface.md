# Statistiques de surface de parc par habitant

Fichiers `data/stats/<dept>/<insee>/stats_<insee>_<année>.json` (commune) et `data/stats/com2co/<id>/stats_c2c_<id>_<urbans|suburbs|all>_<année>.json` (EPCI), servis aussi par `StatsController` (`/map/stats/...`).

Code : `StatsSurfaceRepository` (requêtes) et `StatsSurfaceService` (mise en forme).

## Ce que mesure chaque tranche

Nombre d'habitants selon les m² de parcs **OMS** accessibles à pied par habitant (`surface_park_pcapita_oms` du carreau).

| Commune | Tranches (m²/hab.) |
|---|---|
| Dense (INSEE 1 ou 2) | [0 ; 3[, [3 ; 7[, [7 ; 10[, [10 ; 12[, ≥ 12 |
| Périurbaine | [0 ; 8[, [8 ; 17[, [17 ; 25[, [25 ; 45[, ≥ 45 |

Borne basse incluse, borne haute exclue. Les seuils OMS (`application.business.oms.*`) sont 10 et 12 m² en zone dense, 25 et 45 m² en périurbain.

## Règles de calcul

1. **Ratio OMS.** La tranche d'un carreau vient de `surface_park_pcapita_oms` : seuls les parcs dont le type est OMS (ou forcé par `omsCustom`) comptent. Cimetières, places minérales et parterres sont exclus. Le ratio « tous parcs » (`surface_park_pcapita`) n'est plus utilisé pour les statistiques.
2. **Habitants sans accès.** `pop_exc` (habitants du carreau hors de toute isochrone d'un parc OMS) est ajouté à la **première tranche**, quelle que soit la tranche du carreau. La première tranche regroupe donc les habitants sans parc accessible et ceux qui en ont très peu par habitant.
3. **Carreaux à cheval sur plusieurs communes.** `lcog_geo` vaut `INSEE1_INSEE2`. La population du carreau est répartie entre ces communes au prorata de la surface du carreau dans le cadastre de chacune (`cadastre.geo_shape`), parts normalisées à 1. Une commune d'un EPCI ne compte donc que sa part, et le total de l'EPCI est la somme exacte de ses communes. Sans surface exploitable, le partage est égal.
4. **EPCI.** Les communes denses (`urbans`) et périurbaines (`suburbs`) sont comptées à part, avec leurs tranches propres. `all` regroupe les tranches en trois niveaux OMS (insuffisant, minimum, préconisé).
5. **Pourcentage `ratio`.** Part de la tranche dans le total, à une décimale (chaîne, par exemple `"44.4"`). Le total est la somme des habitants des carreaux calculés.

## Limites à connaître

- La population vient de Filosofi (individus des ménages fiscaux) : elle est inférieure à la population municipale (résidences universitaires, casernes, établissements). L'étude INSEE 2025 s'appuie sur la même source.
- Le prorata au cadastre suppose les habitants répartis uniformément dans le carreau.
- Un code commune de `lcog_geo` absent du cadastre actuel (commune fusionnée depuis le millésime Filosofi) reçoit une part nulle, sa part est redistribuée aux autres communes du carreau.
- Un carreau n'apparaît que s'il a une ligne dans `carre200_computed_v2` : ses jobs sont créés à partir du `code_insee` du carreau (commune de son centroïde). Contrôler la complétude ci-dessous.

## Contrôles à rejouer après chaque recalcul

**Carreaux d'une commune sans calcul** (habitants non comptés) :

```sql
SELECT count(*) AS carreaux_sans_calcul, sum(f.ind) AS habitants_non_comptes
FROM public.filosofi_200m f
WHERE f.annee = :annee
  AND string_to_array(f.lcog_geo, '_') @> ARRAY[:insee]
  AND NOT EXISTS (SELECT 1 FROM public.carre200_computed_v2 cc
                  WHERE cc.annee = f.annee AND cc.id_inspire = f.idcar_200m);
```

**Cohérence EPCI / communes** : pour une année, la somme des `habitants` de `/map/stats/city/{insee}` sur les communes d'un EPCI, restreinte aux communes denses, doit égaler `/map/stats/com2co/{id}/urbans` (idem `suburbs`), à l'arrondi près. Un écart signale un double comptage.

## Persistance liée

`carre200_computed_v2.sustainable_park_pop` contient désormais la population ayant accès à un parc OMS d'au moins `application.business.oms.min_size` m² (5 000 m², soit 0,5 ha) : c'est le critère OMS de taille et de distance. Il était calculé mais non enregistré. Recalculer les carreaux pour renseigner les lignes existantes.
