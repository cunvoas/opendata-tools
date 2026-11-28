# Statistiques de surface de parc par habitant

Fichiers `data/stats/<dept>/<insee>/stats_<insee>_<année>.json` (commune) et `data/stats/com2co/<id>/stats_c2c_<id>_<urbans|suburbs|all>_<année>.json` (EPCI), servis aussi par `StatsController` (`/map/stats/...`).

Un second fichier, suffixé `.v2.json`, est écrit à côté de chaque fichier historique : identité officielle (`codeInsee` ou `siren`), bornes numériques (`borneMin`, `borneMax`, `unite`), `population`, `version` et `miseAJour`. Les fichiers historiques restent servis et portent `"deprecated": true`. Contrat public : `geoservice-data/CONTRAT-DONNEES.md`.

Code : `StatsSurfaceRepository` (requêtes) et `StatsSurfaceService` (mise en forme).

## Ce que mesure chaque tranche

Nombre d'habitants selon les m² de parcs **OMS** accessibles à pied par habitant (`surface_park_pcapita_oms` du carreau).

| Commune | Tranches (m²/hab.) |
|---|---|
| Dense (INSEE 1 ou 2) | [0 ; 3[, [3 ; 7[, [7 ; 10[, [10 ; 12[, ≥ 12 |
| Périurbaine | [0 ; 8[, [8 ; 17[, [17 ; 25[, [25 ; 45[, ≥ 45 |

Borne basse incluse, borne haute exclue. Les seuils (`application.business.oms.*`) sont 10 m² (minimum) et 12 m² (« conseillé ») en zone dense, 25 m² et 45 m² en périurbain. Les minima sont sourcés ; les valeurs conseillées 12 et 45 ne le sont pas, voir « Références à compléter ».

## Règles de calcul

1. **Ratio OMS.** La tranche d'un carreau vient du m²/habitant OMS **recalculé depuis les données brutes** (`surface_access_park_oms / pop_park_share_oms`), et non de `surface_park_pcapita_oms` qui n'a que deux décimales (repli sur cette colonne si la population est nulle) : seuls les parcs dont le type est OMS (ou forcé par `omsCustom`) comptent. Cimetières, places minérales et parterres sont exclus. Le ratio « tous parcs » (`surface_park_pcapita`) n'est plus utilisé pour les statistiques.
2. **Habitants sans accès.** `pop_exc` (habitants du carreau hors de toute isochrone d'un parc OMS) est ajouté à la **première tranche**, quelle que soit la tranche du carreau. La première tranche regroupe donc les habitants sans parc accessible et ceux qui en ont très peu par habitant.
3. **Carreaux à cheval sur plusieurs communes.** `lcog_geo` vaut `INSEE1_INSEE2`. La population du carreau est répartie entre ces communes au prorata de la surface du carreau dans le cadastre de chacune (`cadastre.geo_shape`), parts normalisées à 1. Une commune d'un EPCI ne compte donc que sa part, et le total de l'EPCI est la somme exacte de ses communes. Sans surface exploitable, le partage est égal.
4. **EPCI.** Les communes denses (`urbans`) et périurbaines (`suburbs`) sont comptées à part, avec leurs tranches propres. `all` regroupe les tranches en trois niveaux OMS (insuffisant, minimum, préconisé).
5. **Pourcentage `ratio`.** Part de la tranche dans le total, à une décimale (chaîne, par exemple `"44.4"`) dans le format historique. Le contrat v2 expose un nombre `pourcentage`. Le total est la somme des habitants des carreaux calculés.

## Descripteur du territoire

Chaque JSON porte un bloc `descripteur` avant les tranches, calculé en base au moment de la génération. Il évite de recharger les carreaux : la MEL en compte 17 000.

```json
"descripteur": {
  "habitants": 205789,
  "m2ParHabitantMoyen": 10.95,
  "surfaceParcsOmsM2": 5000,
  "m2ParHabitantGlobal": 52.64
}
```

| Champ | Définition exacte |
|---|---|
| `habitants` | somme des habitants des carreaux (`pop_inc + pop_exc`), au prorata du cadastre, soit la population des tranches |
| `m2ParHabitantMoyen` | **somme(population carreau × m²/habitant du carreau) / somme(population carreau)**. C'est la moyenne, pondérée par la population, des m²/habitant que la carte affiche sur chaque carreau |
| `surfaceParcsOmsM2` | somme des surfaces brutes (`parc_jardin.surface`) des parcs OMS actifs de l'année dont la commune est dans le territoire ; **chaque parc est compté une seule fois** |
| `m2ParHabitantGlobal` | **somme(surface des parcs OMS) / population totale** (`habitants`) |

Précisions :
- Le m²/habitant d'un carreau est celui de la section « Ratio OMS » : surface des parcs OMS accessibles / population dans l'isochrone, non arrondi.
- Les deux moyennes ne se confondent pas : un grand parc dans un quartier peu peuplé relève le ratio global, pas la moyenne vécue par les habitants.
- Les carreaux à cheval sur plusieurs communes sont répartis comme pour les tranches. Un parc est rattaché à sa commune (`parc_jardin.id_city`), même si son isochrone dessert les communes voisines.
- Le caractère OMS d'un parc est celui de l'année (`park_area_computed.oms`) ; un parc fermé avant l'année (`date_fin`) ou ouvert après (`date_debut`) n'est pas compté.
- La population est celle de Filosofi (individus des ménages fiscaux) : le ratio global est donc plus élevé que si l'on divisait par la population municipale de l'INSEE. Ne pas le comparer tel quel à un chiffre calculé sur la population municipale.
- Valeurs arrondies à deux décimales à la fin seulement ; le bloc est absent si le territoire n'a aucune population.

Vérifié sur un jeu de contrôle PostGIS : carreaux à cheval 50/50 et 20/80, parc non OMS, parc fermé, parc d'une autre année, parc sans commune. La somme des tranches égale `habitants`.

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

## Références à compléter

Repère : `@Ref seuils-12-45` (recherche : `grep -rn "@Ref seuils-12-45" geoservice-isochrone-v2 geoservice-map-vue3`).

**Sourcés.** Les minima de 10 m² par habitant (zone centrale) et 25 m² (zone périurbaine) sont les objectifs de la circulaire ministérielle du 8 février 1973, « repris comme norme par l'OMS » selon le Réseau français des Villes-Santé (*Espaces verts urbains : promouvoir l'équité et la santé*, 2020, encadré « Les objectifs chiffrés d'espaces verts par habitant de la circulaire de 1973 »). Le texte précise que le 10 m² vaut pour les parcs et jardins de proximité hors forêts urbaines, et que le 25 m² tient compte des forêts urbaines et des espaces boisés périurbains.

**Sans source identifiée.** Les valeurs « conseillées » de 12 m² (zone dense) et 45 m² (périurbain), ainsi que les libellés « Minimum conseillé par l'OMS » et « Préconisé par l'OMS » des JSON. Elles n'apparaissent ni dans OMS Europe 2016 (*Urban green spaces and health*), ni dans OMS Europe 2017 (*Urban green spaces: a brief for action*), ni dans Villes-Santé 2020 (recherche du 12 novembre 2026 sur les trois documents complets).

**Pour résoudre** : renseigner la source (document, page) à chaque repère, ou retirer la valeur conseillée et ses libellés. Lieux marqués : `application.yml` (`area_reco`), `StatsSurfaceService` (bornes et libellés), page Information (deux phrases publiques). Les documents `docs/*strategy*.md` et `ComputeCarreServiceV4.md` citent aussi 12 m²/hab.

Ce que l'OMS Europe recommande lui-même est un indicateur d'accès : part de la population à moins de 300 m (distance linéaire, environ 5 minutes de marche) d'un espace vert d'au moins 0,5 ha.
