# Scripts SQL — détection et nettoyage des doublons de parcs

Ce dossier contient les scripts d'analyse et de nettoyage des doublons
`parc_jardin` / `park_area`, ainsi que la fonction stockée de nettoyage.

| Fichier | Écriture | Rôle |
|---|---|---|
| `10_park_area_orphelins.sql` | non | `park_area` sans `parc_jardin`, FK cassée, homonymes, noms incohérents |
| `11_park_area_chevauchements.sql` | non | polygones `park_area` qui se recouvrent, invalides, homonymes |
| `12_parc_jardin_chevauchements.sql` | non | homonymes `parc_jardin`, contours qui se recouvrent, points identiques |
| `20_cleanup_park.sql` | **oui** | nettoyage psql avec variables (`-v park_area_id=...`) |
| `30_cleanup_park_function.sql` | DDL | crée la fonction `public.clean_park_duplicate(...)` |
| `pgadmin/20_cleanup_park_pgadmin.sql` | **oui** | même nettoyage, sans variables psql (ids littéraux, `ROLLBACK` par défaut) |
| `pgadmin/README.md` | non | mode d'emploi détaillé pour pgAdmin |

Les scripts de détection sont en **lecture seule**. Les scripts de nettoyage
sont destructifs et nécessitent une sauvegarde préalable.

## 1. Connexion

Les scripts utilisent les variables standard `psql` :

```bash
export PGPASSWORD='...'
export DB_URL='postgresql://user@localhost:5432/isochrone'
psql "$DB_URL" -f scripts/sql/11_park_area_chevauchements.sql
```

En pgAdmin, voir `pgadmin/README.md` (les méta-commandes `\` ne sont pas supportées).

## 2. Détection

### 10 — `park_area` orphelins ou suspects

| Section | Contenu |
|---|---|
| 1a | `park_area.id_parc_jardin IS NULL` |
| 1b | `id_parc_jardin` pointant vers un `parc_jardin` inexistant |
| 1c | `park_area` homonymes (nom normalisé : minuscules, espaces compactés) |
| 1c-bis | homonymes `1c` écartés : périodes disjointes (restructuration) |
| 1d | nom `park_area` = nom `parc_jardin` mais ids différents |
| 1d-bis | paires `1d` écartées : périodes disjointes (restructuration) |
| 1e | `parc_jardin` sans `park_area` |
| 1f | dates `parc_jardin` incohérentes (`date_fin < date_debut`, année hors 1900..année courante, `date_fin` sans `date_debut`, `date_suppr` renseignée) |

Chaque ligne affiche `city` et `epci` via
`parc_jardin.id_city → city.id_comm2co → adm_com2commune`, ainsi que
`date_debut` / `date_fin`.

### Convention de dates (restructurations)

Un parc peut être **restructuré** : l'ancien est clôturé (`date_fin`) et un
nouveau, parfois de même nom, démarre (`date_debut`). Les deux lignes sont
alors légitimes et ne doivent **pas** être fusionnées.

Convention utilisée dans tous les scripts :

- `date_debut` absente → `1900-01-01` (`coalesce`) ;
- `date_fin` absente → `2199-12-31` (`coalesce`).

Les périodes sont donc toujours définies, jamais `NULL`. Deux périodes sont
**disjointes** quand `fin_a < debut_b` ou `fin_b < debut_a`. Ces paires (ou
groupes d'homonymes dont les périodes sont toutes disjointes) ne sont plus
affichées dans les listes de conflits : elles sont reportées dans les sections
`*-bis` (`1c-bis`, `1d-bis`, `2b-bis`, `3a-bis`, `3b-bis`) pour information.
Sur un groupe d'homonymes, la condition est `max(date_debut) <= min(date_fin)`
(propriété des intervalles : intersection commune ⟺ recouvrement deux à deux).

Les dates aberrantes (année 3825, 3915, 2200…) sont listées par la section
**1f** : elles faussent ce calcul et doivent être corrigées avant de conclure.

### 11 — chevauchements `park_area`

| Section | Contenu |
|---|---|
| 2a | polygones invalides (`ST_IsValid = false`), exclus du calcul |
| 2b | paires de polygones qui se recouvrent à ≥ 50 % de la plus petite surface |
| 2b-bis | paires `2b` écartées : périodes disjointes (restructuration) |
| 2c | rappel des homonymes, avec `cities` / `epcis` agrégés (groupes disjoints écartés) |

Colonnes de 2b :

- `area_a_date_debut` / `area_a_date_fin` (idem `b`) : périodes des deux parcs ;
- `pct_petit_recouvert` : part de la plus petite surface couverte par l'autre
  (100 % = le petit polygone est entièrement contenu) ;
- `pct_iou` : intersection sur union, mesure de similarité globale
  (proche de 100 % = quasi-doublon).

Seuil : `0.5` dans la clause `>= 0.5 * LEAST(...)`, modifiable.

### 12 — chevauchements `parc_jardin`

| Section | Contenu |
|---|---|
| 3a | homonymes (nom normalisé), avec `date_debuts` / `date_fins`, `cities` / `epcis` |
| 3a-bis | groupes `3a` écartés : périodes disjointes (restructuration) |
| 3b | contours qui se recouvrent à ≥ 50 % de la plus petite surface |
| 3b-bis | paires `3b` écartées : périodes disjointes (restructuration) |
| 3c | coordonnées identiques (`ST_Equals`), paires disjointes écartées |

Les homonymes génériques (« Aire de jeux », « Pelouse ») sont normaux dans
plusieurs communes : filtrer sur `city` / `epci` avant de conclure.

## 3. Nettoyage

### 3.1 Script psql — `20_cleanup_park.sql`

```bash
# analyse (défaut, aucune écriture)
psql "$DB_URL" -f scripts/sql/20_cleanup_park.sql -v park_area_id=346
psql "$DB_URL" -f scripts/sql/20_cleanup_park.sql -v parc_jardin_id=129

# suppression réelle
psql "$DB_URL" -f scripts/sql/20_cleanup_park.sql -v park_area_id=346 -v confirm=DELETE
```

Un seul des deux paramètres est accepté. `park_area_id` supprime une seule
géométrie ; `parc_jardin_id` supprime le parc **et tous** ses `park_area`.

Avant la section « Contexte », le script affiche la section **0b** : les
`parc_jardin` de nom identique ou préfixe (ex. « Parc X » / « Parc X (avec …) »)
avec leurs dates et `periodes_disjointes`. Si la colonne vaut `true`, il s'agit
d'une restructuration : **arrêter**, ne pas supprimer.

### 3.2 Fonction stockée — recommandée

```bash
psql "$DB_URL" -f scripts/sql/30_cleanup_park_function.sql
```

```sql
-- simulation
SELECT * FROM public.clean_park_duplicate(p_park_area_id => 346);

-- suppression réelle
BEGIN;
SELECT * FROM public.clean_park_duplicate(p_park_area_id => 346, p_dry_run => false);
-- vérifier, puis COMMIT; ou ROLLBACK;
```

Signature :
`clean_park_duplicate(p_park_area_id bigint DEFAULT NULL, p_parc_jardin_id bigint DEFAULT NULL, p_dry_run boolean DEFAULT true)`
→ `TABLE(step text, deleted bigint)`.

Garanties :

- `p_dry_run = true` par défaut : aucune suppression sans argument explicite ;
- refus si zéro ou deux ids sont fournis (`ERRCODE 22023`) ;
- `LOCK TABLE ... SHARE ROW EXCLUSIVE` sur les 8 tables touchées, pour bloquer
  le batch NDVI/calcul concurrent ;
- pas de gestion de transaction interne : `COMMIT`/`ROLLBACK` à la charge de
  l'appelant ;
- `SECURITY INVOKER` : droits de l'appelant ;
- `search_path` figé à `pg_catalog, public` ;
- cible introuvable → `NOTICE` + ligne `aucune_cible / 0` (idempotent).

La fonction **ne vérifie pas les dates** : vérifier d'abord la section 0b de
`20_cleanup_park.sql` (ou les scripts 11/12) pour écarter une restructuration.

### 3.3 Ordre de suppression

Enfants d'abord, parents ensuite :

1. `ndvi_result`
2. `ndvi_observation`
3. `ndvi_job`
4. `park_area_computed`
5. `park_entrance`
6. `park_area`
7. `parc_prefecture` (mode `parc_jardin_id`)
8. `parc_photo` (mode `parc_jardin_id`)
9. `parc_jardin` (mode `parc_jardin_id`)

### 3.4 Sauvegarde préalable

```bash
pg_dump "$DB_URL" -t park_area -t park_entrance -t park_area_computed \
  -t ndvi_job -t ndvi_observation -t ndvi_result -t parc_prefecture -t parc_photo \
  > /tmp/cleanup_backup_$(date +%Y%m%d_%H%M%S).sql
```

## 4. Vérifications après nettoyage

```sql
SELECT id, name, id_parc_jardin FROM public.park_area WHERE id_parc_jardin = 129;

SELECT 'ndvi_job' t, count(*) FROM public.ndvi_job WHERE id = 346
UNION ALL SELECT 'ndvi_observation', count(*) FROM public.ndvi_observation WHERE park_area_id = 346
UNION ALL SELECT 'park_area_computed', count(*) FROM public.park_area_computed WHERE id = 346
UNION ALL SELECT 'park_entrance', count(*) FROM public.park_entrance WHERE area_id = 346
UNION ALL SELECT 'ndvi_result', count(*) FROM public.ndvi_result WHERE park_area_computed_id = 346;
```

Relancer ensuite les scripts `10`, `11`, `12`.

## 5. Dépendances et limites

Dépendances réelles (FK) :

- `park_entrance.area_id → park_area.id`
- `parc_prefecture.id_parc → parc_jardin.identifiant`

Dépendances logiques sans FK, nettoyées explicitement par les scripts :

- `park_area_computed.id = park_area.id`
- `ndvi_job.id = park_area.id`
- `ndvi_observation.park_area_id = park_area.id`
- `ndvi_result.park_area_computed_id = park_area.id`
- `ndvi_result.parc_et_jardin_id = parc_jardin.identifiant`
- `parc_photo.parc_id = parc_jardin.identifiant`

Une future table référençant `park_area.id` ou `parc_jardin.identifiant` devra
être ajoutée aux scripts et à la fonction, sinon elle restera orpheline.

Les calculs de chevauchement utilisent `ST_Intersection` sur des géométries
4326 ; les polygones invalides sont listés à part et exclus. Un chevauchement
fort ne prouve pas un doublon : vérifier nom, ville et historique avant
suppression.
