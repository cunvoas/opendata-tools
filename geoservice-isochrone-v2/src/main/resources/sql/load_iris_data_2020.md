# Chargement des données IRIS 2020 — `load_iris_data_2020.sql`

## Contexte

Ce script charge en masse les données démographiques et sociales des IRIS (Ilots Regroupés pour l'Information Statistique) pour l'année **2020** dans la table `iris_data`.

Source INSEE : [Base IC — Évolution et structure de la population 2020](https://www.insee.fr/fr/statistiques/7704076)

---

## Prérequis

| Élément | Valeur attendue |
|---|---|
| SGBD | PostgreSQL ≥ 13 |
| Table cible | `iris_data` (créée par le schéma JPA de l'application) |
| Fichier source | `iris_base-ic-evol-struct-pop-2020.CSV` |
| Encodage CSV | UTF-8 |
| Séparateur CSV | `;` |
| Lignes de données | ~49 273 |

---

## Téléchargement du fichier source

```bash
# INSEE — Base IC évolution et structure de la population 2020
wget -O /work/PERSO/ASSO/data/iris_base-ic-evol-struct-pop-2020.CSV \
  "https://www.insee.fr/fr/statistiques/fichier/7704076/base-ic-evol-struct-pop-2020.CSV"
```

---

## Structure du mapping CSV → table

Le CSV INSEE utilise des préfixes d'année sur les colonnes numériques ; la table `iris_data` n'en a pas.

| Colonne CSV | Colonne `iris_data` | Description |
|---|---|---|
| `IRIS` | `iris` | Code IRIS (clé primaire partielle) |
| `COM` | `com` | Code INSEE commune |
| `TYP_IRIS` | `typ_iris` | Type d'IRIS (H/A/D/Z) |
| `LAB_IRIS` | `lab_iris` | Indicateur qualité |
| `P20_POP` | `pop` | Population totale |
| `P20_POP0002` … `P20_POP80P` | `pop0002` … `pop80p` | Population par tranche d'âge fine |
| `P20_POP0014` … `P20_POP65P` | `pop0014` … `pop65p` | Population par tranche d'âge large |
| `P20_POPH` | `poph` | Population masculine |
| `P20_H0014` … `P20_H65P` | `h0014` … `h65p` | Hommes par tranche d'âge |
| `P20_POPF` | `popf` | Population féminine |
| `P20_F0014` … `P20_F65P` | `f0014` … `f65p` | Femmes par tranche d'âge |
| `C20_POP15P` | `pop15p` | Pop. 15 ans ou plus |
| `C20_POP15P_CS1` … `C20_POP15P_CS8` | `pop15p_cs1` … `pop15p_cs8` | Pop. 15+ par CSP (total) |
| `C20_H15P_CS1` … `C20_H15P_CS8` | `h15p_cs1` … `h15p_cs8` | Pop. 15+ par CSP (hommes) |
| `C20_F15P_CS1` … `C20_F15P_CS8` | `f15p_cs1` … `f15p_cs8` | Pop. 15+ par CSP (femmes) |
| `P20_POP_FR` | `pop_fr` | Population française |
| `P20_POP_ETR` | `pop_etr` | Population étrangère |
| `P20_POP_IMM` | `pop_imm` | Population immigrée |
| `P20_PMEN` | `pmen` | Population des ménages |
| `P20_PHORMEN` | `phormen` | Population hors ménages |
| *(absent du CSV)* | `annee` | Injecté en dur : `2020` |

### Codes CSP

| Code | Catégorie socioprofessionnelle |
|---|---|
| CS1 | Agriculteurs exploitants |
| CS2 | Artisans, commerçants, chefs d'entreprise |
| CS3 | Cadres et professions intellectuelles supérieures |
| CS4 | Professions intermédiaires |
| CS5 | Employés |
| CS6 | Ouvriers |
| CS7 | Retraités |
| CS8 | Autres personnes sans activité professionnelle |

---

## Exécution

### Depuis le serveur PostgreSQL (COPY serveur)

Le `COPY` natif lit le fichier **côté serveur**. Le chemin doit être absolu et accessible par le processus `postgres`.

```bash
psql -U <user> -d <dbname> \
     -f src/main/resources/sql/load_iris_data_2020.sql
```

> Le chemin du fichier est câblé dans le script :
> `/work/PERSO/ASSO/data/iris_base-ic-evol-struct-pop-2020.CSV`
> Adapter si nécessaire avant l'exécution.

### Depuis un client psql (fichier côté client)

Remplacer `COPY` par `\copy` dans le script si le fichier est sur la machine cliente et non sur le serveur :

```sql
-- Remplacer dans le script :
COPY staging_iris_2020 ( ... )
FROM '/chemin/local/iris_base-ic-evol-struct-pop-2020.CSV'
...

-- Par :
\copy staging_iris_2020 ( ... )
FROM '/chemin/local/iris_base-ic-evol-struct-pop-2020.CSV'
...
```

`\copy` est une commande psql client ; elle ne requiert pas de droits `SUPERUSER`.

### Via Docker

```bash
# Copier le CSV dans le conteneur puis exécuter
docker cp /work/PERSO/ASSO/data/iris_base-ic-evol-struct-pop-2020.CSV \
          <container>:/tmp/

# Adapter le chemin dans le script, puis :
docker exec -i <container> \
  psql -U <user> -d <dbname> \
  -f /chemin/vers/load_iris_data_2020.sql
```

---

## Comportement idempotent

Le script utilise `ON CONFLICT (annee, iris) DO UPDATE` : il peut être relancé plusieurs fois sans créer de doublons. Les lignes existantes pour `annee = 2020` sont mises à jour.

---

## Vérification post-chargement

```sql
-- Nombre total de lignes chargées pour 2020
SELECT COUNT(*) FROM iris_data WHERE annee = 2020;
-- Attendu : ~49 273

-- Contrôle de cohérence par type d'IRIS
SELECT typ_iris, COUNT(*) 
FROM iris_data 
WHERE annee = 2020 
GROUP BY typ_iris 
ORDER BY typ_iris;

-- Vérification d'une commune (ex. Lille = 59350)
SELECT iris, pop, poph, popf
FROM iris_data
WHERE annee = 2020 AND com = '59350'
ORDER BY iris;
```

---

## Stratégie technique

```
CSV (colonnes P20_/C20_)
        │
        ▼ COPY massif (PostgreSQL natif)
  staging_iris_2020   ← table TEMP TEXT (droppée au COMMIT)
        │
        ▼ INSERT … SELECT + NULLIF cast + annee=2020
    iris_data
```

1. **Table de staging `TEXT`** — évite tout rejet de type à l'import (valeurs vides, décimales `.` vs `,`).
2. **`COPY FROM … WITH (FORMAT CSV, DELIMITER ';', HEADER TRUE)`** — import natif PostgreSQL, le plus rapide disponible.
3. **`NULLIF(col, '')::NUMERIC(16,4)`** — convertit proprement les champs vides en `NULL`.
4. **`ON COMMIT DROP`** — la table temporaire est supprimée automatiquement à la fin de la transaction, sans nettoyage explicite.

---

## Chargement d'autres années

Pour charger une autre année (ex. 2017), dupliquer le script et adapter :

- Le nom du fichier CSV source
- La valeur littérale `2020` → `2017` dans le `INSERT`
- Les préfixes `P20_` / `C20_` → `P17_` / `C17_` dans la table de staging et le `SELECT`

> Vérifier le dictionnaire des variables INSEE pour chaque millésime, les colonnes disponibles peuvent varier.
