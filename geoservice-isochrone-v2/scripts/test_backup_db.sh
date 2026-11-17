#!/usr/bin/env bash
# Teste un backup produit par backup_restore_db.sh :
#   1. vérifie que chaque .sql.gz attendu existe, n'est pas vide et est un gzip valide
#   2. restaure le schéma puis les données dans une base de TEST
#   3. compare le nombre de lignes source / cible, table par table
#
# Usage :
#   ./test_backup_db.sh -o OUTDIR -d DB_SOURCE --target-db DB_TEST \
#                       [-h HOST] [-p PORT] [-U USER] [-s SCHEMA] [--opendata] [--reset]
#
#   --target-db  base de test (doit exister ; doit être vide sauf avec --reset)
#   --reset      DROP SCHEMA public CASCADE puis CREATE SCHEMA public sur la cible
#   --opendata   même flag que lors du backup (inclut les tables opendata)
#
# Env vars : PGPASSWORD (optionnel, utilisé pour source et cible)
#
# Prérequis : base cible avec PostGIS disponible (CREATE EXTENSION dans le schéma sauvegardé).
set -e
set -o pipefail

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)

SCHEMA="public"
DB=""
TARGET_DB=""
HOST="localhost"
PORT="5432"
USER="postgres"
OUTDIR=""
OPENDATA=false
RESET=false

usage() {
  echo "Usage: $0 -o OUTDIR -d DB_SOURCE --target-db DB_TEST [-h HOST] [-p PORT] [-U USER] [-s SCHEMA] [--opendata] [--reset]"
  exit 1
}

while [ $# -gt 0 ]; do
  case "$1" in
    -d) DB="$2"; shift 2 ;;
    -t|--target-db) TARGET_DB="$2"; shift 2 ;;
    -h) HOST="$2"; shift 2 ;;
    -p) PORT="$2"; shift 2 ;;
    -U) USER="$2"; shift 2 ;;
    -s) SCHEMA="$2"; shift 2 ;;
    -o) OUTDIR="$2"; shift 2 ;;
    --opendata) OPENDATA=true; shift ;;
    --reset) RESET=true; shift ;;
    *) usage ;;
  esac
done

[ -z "$DB" ]        && { echo "ERROR: -d DB_SOURCE required" >&2; usage; }
[ -z "$TARGET_DB" ] && { echo "ERROR: --target-db DB_TEST required" >&2; usage; }
[ -z "$OUTDIR" ]    && { echo "ERROR: -o OUTDIR required" >&2; usage; }
[ -d "$OUTDIR" ]    || { echo "ERROR: OUTDIR not found: $OUTDIR" >&2; exit 1; }
[ "$DB" = "$TARGET_DB" ] && { echo "ERROR: source and target DB must differ" >&2; exit 1; }

SRC_OPTS="-h $HOST -p $PORT -U $USER -d $DB"
TGT_OPTS="-h $HOST -p $PORT -U $USER -d $TARGET_DB"

# Liste des tables : réutilise le script de backup (évite la duplication).
LIST_ARGS="-d $DB -h $HOST -p $PORT -U $USER"
[ "$OPENDATA" = true ] && LIST_ARGS="$LIST_ARGS --opendata"
TABLES=$(bash "$SCRIPT_DIR/backup_restore_db.sh" list $LIST_ARGS | sed -n 's/^  \[[0-9]*\] //p')
[ -n "$TABLES" ] || { echo "ERROR: no table list from backup_restore_db.sh" >&2; exit 1; }

echo "=== 1. Vérification des fichiers de $OUTDIR/ ==="
ERRORS=0
schema_file="$OUTDIR/00_schema.sql.gz"
if [ ! -f "$schema_file" ]; then
  echo "  MANQUANT : $schema_file"; ERRORS=$((ERRORS + 1))
elif [ ! -s "$schema_file" ]; then
  echo "  VIDE     : $schema_file"; ERRORS=$((ERRORS + 1))
elif ! gzip -t "$schema_file" 2>/dev/null; then
  echo "  GZIP KO  : $schema_file"; ERRORS=$((ERRORS + 1))
fi
for tbl in $TABLES; do
  f="$OUTDIR/${tbl}.sql.gz"
  if [ ! -f "$f" ]; then
    echo "  MANQUANT : $f"; ERRORS=$((ERRORS + 1)); continue
  fi
  if [ ! -s "$f" ]; then
    echo "  VIDE     : $f"; ERRORS=$((ERRORS + 1)); continue
  fi
  if ! gzip -t "$f" 2>/dev/null; then
    echo "  GZIP KO  : $f"; ERRORS=$((ERRORS + 1)); continue
  fi
done
echo "  fichiers KO : $ERRORS"
[ "$ERRORS" -eq 0 ] || { echo "ABORT: backup incomplet ou corrompu"; exit 1; }

echo "=== 2. Préparation de la base cible $TARGET_DB ==="
nb_tables=$(psql $TGT_OPTS -tA -c \
  "SELECT count(*) FROM information_schema.tables WHERE table_schema='$SCHEMA'")
if [ "$RESET" = true ]; then
  echo "  --reset : suppression de tous les objets de la base cible"
  # postgis est installé dans public : le supprimer avant de recréer le schéma.
  psql $TGT_OPTS -v ON_ERROR_STOP=1 -q \
    -c "DROP EXTENSION IF EXISTS postgis CASCADE;" \
    -c "DROP SCHEMA IF EXISTS $SCHEMA CASCADE;" \
    -c "CREATE SCHEMA $SCHEMA;"
elif [ "$nb_tables" -ne 0 ]; then
  echo "  ERROR: la base cible contient déjà $nb_tables tables dans $SCHEMA." >&2
  echo "  Utiliser une base vide ou --reset (destructif)."
  exit 1
fi

if [ "$(psql $TGT_OPTS -tA -c "SELECT count(*) FROM pg_extension WHERE extname='postgis'")" = "0" ]; then
  echo "  création de l'extension postgis ..."
  psql $TGT_OPTS -v ON_ERROR_STOP=1 -q -c "CREATE EXTENSION postgis;"
fi

echo "=== 3. Restauration dans la base cible ==="
echo "  schéma ..."
gunzip -c "$schema_file" \
  | grep -v "^CREATE SCHEMA ${SCHEMA};$" \
  | psql $TGT_OPTS -v ON_ERROR_STOP=1 -q

for tbl in $TABLES; do
  echo "  données: $tbl ..."
  gunzip -c "$OUTDIR/${tbl}.sql.gz" | psql $TGT_OPTS -v ON_ERROR_STOP=1 -q
done

echo "=== 4. Comparaison des nombres de lignes (source / cible) ==="
MISMATCH=0
for tbl in $TABLES; do
  src=$(psql $SRC_OPTS -tA -c "SELECT count(*) FROM $SCHEMA.$tbl")
  tgt=$(psql $TGT_OPTS -tA -c "SELECT count(*) FROM $SCHEMA.$tbl")
  if [ "$src" = "$tgt" ]; then
    printf '  OK      %-28s %s\n' "$tbl" "$src"
  else
    printf '  DIFF    %-28s source=%s cible=%s\n' "$tbl" "$src" "$tgt"
    MISMATCH=$((MISMATCH + 1))
  fi
done

echo ""
if [ "$MISMATCH" -ne 0 ]; then
  echo "ECHEC: $MISMATCH table(s) avec un écart de lignes."
  exit 1
fi
echo "SUCCES: $(echo "$TABLES" | wc -l) tables restaurées, compteurs identiques."
