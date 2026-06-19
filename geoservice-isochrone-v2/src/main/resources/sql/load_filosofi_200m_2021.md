# Chargement des données Filosofi 200m 2021 — `filosofi_200m`

## Contexte

Ce jeu de scripts charge en masse les données de la base **Filosofi 2021** à l'échelle des carreaux de 200 m dans la table `filosofi_200m`.

La table est **partitionnée par LIST sur `annee`** : chaque millésime dispose de sa propre partition physique. Le script de partition crée la partition 2021 ; le script de chargement y injecte les données.

Source INSEE : [Filosofi 2021 — carreaux de 200 m](https://www.insee.fr/fr/statistiques/7655673)

---

## Fichiers concernés

| Script | Rôle |
|---|---|
| `partition_filosofi_200m_2021.sql` | Crée la partition `filosofi_200m_2021` |
| `load_filosofi_200m_2021.sql` | Charge les 3 fichiers CSV régionaux |

---

## Prérequis

| Élément | Valeur attendue |
|---|---|
| SGBD | PostgreSQL ≥ 13 |
| Table parente | `filosofi_200m` partitionnée `BY LIST (annee)` |
| Table de staging | `filosofi_load` (créée par `filosofil.sql`) |
| Répertoire source | `Filosofi2021_carreaux_200m_csv/` |
| Encodage CSV | ASCII (compatible UTF-8) |
| Séparateur CSV | `,` |

### Partitions existantes avant 2021

```
filosofi_200m_2015  → VALUES IN (2015)
filosofi_200m_2017  → VALUES IN (2017)
filosofi_200m_2019  → VALUES IN (2019)
```

---

## Téléchargement des fichiers source

```bash
# INSEE — Filosofi 2021 carreaux 200 m
# https://www.insee.fr/fr/statistiques/7655673
BASE=/work/PERSO/ASSO/data/INSEE/Filosofi2021_carreaux_200m_csv
mkdir -p "$BASE"

# Les fichiers sont fournis dans un ZIP par l'INSEE
# Décompresser l'archive téléchargée :
unzip Filosofi2021_carreaux_200m_csv.zip -d "$BASE"
```

### Fichiers attendus

| Fichier | Territoire | Lignes (~) |
|---|---|---|
| `carreaux_200m_met.csv` | Métropole | 2 298 582 |
| `carreaux_200m_mart.csv` | Martinique | 11 222 |
| `carreaux_200m_reun.csv` | La Réunion | 14 773 |
| **Total** | | **2 324 577** |

---

## Structure du mapping CSV → table

Les colonnes CSV 2021 correspondent directement aux colonnes de `filosofi_200m`. Seule différence notable avec les millésimes 2017/2019 : **la colonne `groupe` est absente du CSV 2021** (stockée `NULL` en base).

| Colonne CSV | Colonne `filosofi_200m` | Description |
|---|---|---|
| `idcar_200m` | `idcar_200m` | Identifiant INSPIRE du carreau 200 m (clé primaire partielle) |
| `idcar_1km` | `idcar_1km` | Identifiant INSPIRE du carreau 1 km parent |
| `idcar_nat` | `idcar_nat` | Identifiant INSPIRE du carreau de niveau naturel |
| `i_est_200` | `i_est_200` | Carreau approché à 200 m (0/1) |
| `i_est_1km` | `i_est_1km` | Carreau approché à 1 km (0/1) |
| `lcog_geo` | `lcog_geo` | Code officiel géographique (commune) |
| `ind` | `ind` | Nombre d'individus |
| `men` | `men` | Nombre de ménages |
| `men_pauv` | `men_pauv` | Ménages pauvres |
| `men_1ind` | `men_1ind` | Ménages d'un seul individu |
| `men_5ind` | `men_5ind` | Ménages de 5 individus ou plus |
| `men_prop` | `men_prop` | Ménages propriétaires |
| `men_fmp` | `men_fmp` | Ménages monoparentaux |
| `ind_snv` | `ind_snv` | Somme des niveaux de vie winsorisés |
| `men_surf` | `men_surf` | Somme des surfaces des logements |
| `men_coll` | `men_coll` | Ménages en logements collectifs |
| `men_mais` | `men_mais` | Ménages en maison |
| `log_av45` | `log_av45` | Logements construits avant 1945 |
| `log_45_70` | `log_45_70` | Logements 1945–1969 |
| `log_70_90` | `log_70_90` | Logements 1970–1989 |
| `log_ap90` | `log_ap90` | Logements depuis 1990 |
| `log_inc` | `log_inc` | Logements date inconnue |
| `log_soc` | `log_soc` | Logements sociaux |
| `ind_0_3` | `ind_0_3` | Individus 0–3 ans |
| `ind_4_5` | `ind_4_5` | Individus 4–5 ans |
| `ind_6_10` | `ind_6_10` | Individus 6–10 ans |
| `ind_11_17` | `ind_11_17` | Individus 11–17 ans |
| `ind_18_24` | `ind_18_24` | Individus 18–24 ans |
| `ind_25_39` | `ind_25_39` | Individus 25–39 ans |
| `ind_40_54` | `ind_40_54` | Individus 40–54 ans |
| `ind_55_64` | `ind_55_64` | Individus 55–64 ans |
| `ind_65_79` | `ind_65_79` | Individus 65–79 ans |
| `ind_80p` | `ind_80p` | Individus 80 ans ou plus |
| `ind_inc` | `ind_inc` | Individus âge inconnu |
| *(absent)* | `groupe` | `NULL` — absent du CSV 2021 |
| *(absent)* | `annee` | Injecté en dur : `2021` |

---

## Exécution

### Ordre des scripts

```bash
# 1. Créer la partition (une seule fois)
psql -U <user> -d <dbname> -f partition_filosofi_200m_2021.sql

# 2. Charger les données (relançable)
psql -U <user> -d <dbname> -f load_filosofi_200m_2021.sql
```

> Les deux scripts utilisent des transactions ; en cas d'erreur,
> tout le chargement est annulé proprement.

### Via Docker

```bash
# Rendre le répertoire data accessible dans le conteneur
docker run --rm \
  -v /work/PERSO/ASSO/data:/data \
  -v $(pwd)/src/main/resources/sql:/sql \
  postgres:16 \
  psql -h <host> -U <user> -d <dbname> -f /sql/partition_filosofi_200m_2021.sql

docker run --rm \
  -v /work/PERSO/ASSO/data:/data \
  -v $(pwd)/src/main/resources/sql:/sql \
  postgres:16 \
  psql -h <host> -U <user> -d <dbname> -f /sql/load_filosofi_200m_2021.sql
```

> Adapter le chemin `/work/PERSO/ASSO/data` dans le script si le fichier est monté ailleurs.

---

## Comportement idempotent

Le script de chargement commence par :

```sql
DELETE FROM filosofi_200m WHERE annee = 2021;
```

Il peut être **relancé sans risque de doublons**. Les partitions rendent ce `DELETE` très rapide (opération sur la partition seule).

---

## Stratégie technique

```
CSV Métropole  (2,3M)  ─┐
CSV Martinique (~11K)  ─┤──► TRUNCATE filosofi_load
CSV Réunion    (~15K)  ─┘         │
                                   ▼ COPY (natif PostgreSQL, délimiteur ,)
                            filosofi_load   ← table de staging permanente
                                   │
                                   ▼ INSERT … SELECT 2021, …
                            filosofi_200m_2021  ← partition LIST(2021)
```

- **`filosofi_load`** : table de staging partagée du projet, réutilisée par chaque millésime. Le `TRUNCATE` entre chaque fichier est nécessaire pour ne pas cumuler les territoires.
- **`groupe = NULL`** : la colonne `groupe` (présente en 2017) a disparu du CSV 2021. Elle est insérée comme `NULL`, ce qui est cohérent avec l'évolution du format INSEE.
- **`FORCE_NOT_NULL`** : les trois clés `idcar_200m`, `idcar_1km`, `idcar_nat` sont forcées non-nulles à l'import pour rejeter immédiatement tout enregistrement invalide.

---

## Vérification post-chargement

```sql
-- Nombre total de carreaux chargés pour 2021
SELECT COUNT(*) FROM filosofi_200m WHERE annee = 2021;
-- Attendu : ~2 324 577

-- Répartition par territoire (via préfixe de l'identifiant INSPIRE)
SELECT
    CASE
        WHEN idcar_200m LIKE 'CRS3035%' THEN 'Métropole'
        WHEN idcar_200m LIKE 'CRS5490%' THEN 'DOM (Martinique/Réunion)'
        ELSE 'Autre'
    END AS territoire,
    COUNT(*) AS nb_carreaux
FROM filosofi_200m
WHERE annee = 2021
GROUP BY 1;

-- Contrôle de cohérence : carreaux approchés vs réels
SELECT i_est_200, COUNT(*)
FROM filosofi_200m
WHERE annee = 2021
GROUP BY i_est_200;

-- Exemple : carreaux de Lille (59350)
SELECT idcar_200m, ind, men, ind_snv
FROM filosofi_200m
WHERE annee = 2021
  AND lcog_geo = '59350'
ORDER BY idcar_200m
LIMIT 10;
```

---

## Différences entre millésimes

| Aspect | 2015 | 2017 | 2019 | 2021 |
|---|---|---|---|---|
| Colonne `groupe` | ✓ | ✓ | ✓ | ✗ (absent) |
| Encodage CSV | LATIN1 | LATIN1 | LATIN1 | ASCII/UTF-8 |
| Territoires (fichiers) | 1 | 1 | 1 | 3 (MET + MART + REUN) |
| Table de staging | via autre table | `filosofi_load` | `filosofi_load` | `filosofi_load` |

---

## Winsorisation — note méthodologique

Le champ `ind_snv` (somme des niveaux de vie) est **winsorisé** par l'INSEE : les valeurs extrêmes de la distribution sont ramenées à un seuil quantile pour limiter l'effet des outliers. Ce n'est pas un niveau de vie moyen mais une **somme brute winsorisée** qui nécessite d'être divisée par `ind` pour obtenir un niveau de vie moyen par carreau.

```sql
-- Niveau de vie moyen par carreau (€/individu)
SELECT idcar_200m, lcog_geo,
       ROUND(ind_snv / NULLIF(ind, 0), 2) AS niv_vie_moyen
FROM filosofi_200m
WHERE annee = 2021
  AND lcog_geo = '59350'
ORDER BY niv_vie_moyen DESC;
```
