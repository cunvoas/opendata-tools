# Scripts SQL — usage dans pgAdmin

Ces scripts servent à **détecter** puis **nettoyer** les doublons de parcs
(`park_area` / `parc_jardin`). Les scripts `10`, `11`, `12` et `20` du dossier
parent sont écrits pour `psql` : pgAdmin ne comprend pas les méta-commandes
`\echo`, `\pset`, `\set`, `\if` ni les variables `:park_area_id`.

## 1. Prérequis

- pgAdmin 4 connecté à la base (Query Tool).
- Droits `SELECT` pour l'analyse, `DELETE` sur les tables concernées pour le nettoyage.
- **Faire une sauvegarde avant toute suppression** :

```bash
pg_dump "$DB_URL" -t park_area -t park_entrance -t park_area_computed \
  -t ndvi_job -t ndvi_observation -t ndvi_result -t parc_prefecture -t parc_photo \
  > /tmp/cleanup_backup_$(date +%Y%m%d_%H%M%S).sql
```

Dans pgAdmin : clic droit sur la base → *Backup* → format *Plain* ou *Custom*.

## 2. Scripts de détection (lecture seule)

Ouvrir le fichier dans le Query Tool et **supprimer les lignes qui commencent par `\`**
(ce sont les seules incompatibilités pgAdmin).

Alternative en ligne de commande pour générer une copie prête à coller :

```bash
grep -v '^\\' scripts/sql/11_park_area_chevauchements.sql > /tmp/11_pgadmin.sql
```

| Fichier | Détecte |
|---|---|
| `scripts/sql/10_park_area_orphelins.sql` | `park_area` sans `parc_jardin`, FK cassée, homonymes, noms incohérents |
| `scripts/sql/11_park_area_chevauchements.sql` | polygones `park_area` qui se recouvrent ≥ 50 %, invalides, homonymes |
| `scripts/sql/12_parc_jardin_chevauchements.sql` | `parc_jardin` homonymes, contours qui se recouvrent ≥ 50 %, points identiques |

Chaque requête est indépendante : on peut ne copier que la section voulue dans
le Query Tool. Seuil de recouvrement = `0.5` dans les sections 2b/3b.

Colonnes utiles : `city`, `epci`, `pct_petit_recouvert` (% de la plus petite
surface recouverte) et `pct_iou` (similarité globale des deux géométries).

## 3. Nettoyage (écriture)

Script dédié pgAdmin : `scripts/sql/pgadmin/20_cleanup_park_pgadmin.sql`.

1. Ouvrir le fichier dans le Query Tool.
2. Éditer **uniquement** le bloc `CIBLE` :
   ```sql
   SELECT 346::bigint AS park_area_id,    -- id park_area à supprimer, sinon NULL
          NULL::bigint AS parc_jardin_id  -- id parc_jardin à supprimer, sinon NULL
   ```
   Un seul des deux doit être non `NULL`. `parc_jardin_id` supprime aussi tous
   ses `park_area` et leurs dépendances.
3. Exécuter tout le script. Il se termine par **`ROLLBACK`** : aucune
   suppression n'est conservée.
4. Lire les compteurs `DELETE n` affichés. Ils donnent le nombre de lignes par table.
5. Si tout est correct, remplacer `ROLLBACK;` par `COMMIT;` en fin de script et
   ré-exécuter.

Le script affiche aussi, avant les suppressions, les `parc_jardin` homonymes
(nom identique ou préfixe) avec leurs `date_debut` / `date_fin` et
`periodes_disjointes`. `true` = les deux parcs ne se sont pas chevauchés dans
le temps (restructuration) → **ne pas supprimer**. Convention : `date_debut`
absente = `1900-01-01`, `date_fin` absente = `2199-12-31`.

Ordre des suppressions (une seule transaction, tout ou rien) :

1. `ndvi_result`
2. `ndvi_observation`
3. `ndvi_job`
4. `park_area_computed`
5. `park_entrance`
6. `park_area`
7. `parc_prefecture`, `parc_photo`, `parc_jardin` (seulement en mode `parc_jardin_id`)

Un `LOCK TABLE ... IN SHARE ROW EXCLUSIVE MODE` bloque les écritures du batch
NDVI/calcul pendant l'opération. En cas d'erreur, la transaction est annulée
automatiquement : rien n'est supprimé.

## 3bis. Variante fonction stockée (recommandée)

Le script `scripts/sql/30_cleanup_park_function.sql` crée une fonction
`public.clean_park_duplicate(p_park_area_id, p_parc_jardin_id, p_dry_run)`.
Une fois installée, elle s'utilise directement dans le Query Tool :

```sql
-- simulation (p_dry_run = true par défaut) : aucun suppression
SELECT * FROM public.clean_park_duplicate(p_park_area_id => 346);
SELECT * FROM public.clean_park_duplicate(p_parc_jardin_id => 129);

-- suppression réelle dans la transaction courante
BEGIN;
SELECT * FROM public.clean_park_duplicate(p_park_area_id => 346, p_dry_run => false);
-- vérifier les compteurs, puis COMMIT; ou ROLLBACK;
```

Elle retourne une ligne par étape (`ndvi_result`, `ndvi_observation`,
`ndvi_job`, `park_area_computed`, `park_entrance`, `park_area`, puis
`parc_prefecture`, `parc_photo`, `parc_jardin` si `parc_jardin_id`).
Elle refuse les appels avec zéro ou deux ids (`ERRCODE 22023`), pose les
verrous `SHARE ROW EXCLUSIVE` et ne gère pas la transaction elle-même.

## 4. Vérifications après nettoyage

Relancer les scripts de détection, ou directement :

```sql
-- plus aucun chevauchement du doublon supprimé
SELECT id, name, id_parc_jardin FROM public.park_area
WHERE id_parc_jardin = 129;

-- aucune ligne orpheline sur l'id supprimé
SELECT 'ndvi_job' t, count(*) FROM public.ndvi_job WHERE id = 346
UNION ALL SELECT 'ndvi_observation', count(*) FROM public.ndvi_observation WHERE park_area_id = 346
UNION ALL SELECT 'park_area_computed', count(*) FROM public.park_area_computed WHERE id = 346
UNION ALL SELECT 'park_entrance', count(*) FROM public.park_entrance WHERE area_id = 346
UNION ALL SELECT 'ndvi_result', count(*) FROM public.ndvi_result WHERE park_area_computed_id = 346;
```

## 5. Retour arrière

- Avant `COMMIT` : rien à faire, le `ROLLBACK` a annulé.
- Après `COMMIT` : restaurer les tables depuis la sauvegarde :
  ```bash
  pg_restore -d "$DB_URL" --data-only --table=park_area --table=park_entrance \
    --table=park_area_computed --table=ndvi_job --table=ndvi_observation \
    --table=ndvi_result /tmp/cleanup_backup_XXXX.sql
  ```
  ou via pgAdmin : clic droit sur la base → *Restore*.

## 6. Différences pgAdmin / psql

| psql | pgAdmin |
|---|---|
| `\set`, `\if`, `:variable` | non supporté → utiliser le script `pgadmin/` à ids littéraux |
| `\echo`, `\pset` | non supporté → supprimer les lignes `\...` |
| `psql -f fichier.sql` | Query Tool → bouton *Ouvrir un fichier*, puis *Exécuter* (F5) |
| `$DB_URL` | choisir la connexion dans l'arbre pgAdmin |

Les scripts du dossier parent restent la référence pour les exécutions
automatisées (`psql`, CI, cron).
