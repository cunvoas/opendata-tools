#!/bin/sh
# Backup/Restore PostgreSQL database - one gzipped SQL file per table
# Usage:
#   ./backup_restore_db.sh backup   -d DB -h HOST -p PORT -U USER [-s SCHEMA] [-o OUTDIR]
#   ./backup_restore_db.sh restore  -d DB -h HOST -p PORT -U USER [-s SCHEMA] [-o OUTDIR] [-b BATCH]
#   ./backup_restore_db.sh list     -d DB -h HOST -p PORT -U USER
#
# Env vars: PGPASSWORD (optional)

set -e

# ─── Defaults ───────────────────────────────────────────────────────────────
SCHEMA="public"
DB=""
HOST="localhost"
PORT="5432"
USER="postgres"
OUTDIR="./${DB}/$(date +%Y%m%d)"
BATCH=1000
OPENDATA=false

# ─── Topological order for restore (parents before children) ──────────────
# Level OPENDATA: no FK dependencies
TABLES_OPENDATA="
  cadastre
  laposte
  insee_densite_city
  filosofi_200m
  park_overpass
  carre200onlyshape
  ign_topo_vegetal
"

# Level 0: no FK dependencies
TABLES_L0="
  adm_region
  park_type
  adm_asso
  park_proposal_meta
  dashboard_cache
  parc_photo
  cadastre_proche
  z_stats_surface
  compute_job
  compute_iris_job
  adm_activity_stats
  adm_contrib_action
  park_area_computed
  iris_data_computed
  carre200_computed_v2
  project_simul
  project_simul_isochrone
  project_simul_work
  park_proposal_work
"
# Level 1: FK to Level 0
TABLES_L1="
  adm_com2commune
  adm_departement
  park_area
  adm_contrib
"
# Level 2: FK to Level 0-1
TABLES_L2="
  city
"
# Level 3: FK to Level 0-2
TABLES_L3="
  parc_jardin
  park_entrance
  park_proposal
"
# Level 4: FK to Level 0-3
TABLES_L4="
  parc_prefecture
"

ALL_TABLES="${TABLES_L0} ${TABLES_L1} ${TABLES_L2} ${TABLES_L3} ${TABLES_L4}"
RESTORE_ORDER="${TABLES_L0} ${TABLES_L1} ${TABLES_L2} ${TABLES_L3} ${TABLES_L4}"

# ─── Parse args ────────────────────────────────────────────────────────────
MODE=""
while [ $# -gt 0 ]; do
  case "$1" in
    backup|restore|list) MODE="$1"; shift ;;
    -d) DB="$2"; shift 2 ;;
    -h) HOST="$2"; shift 2 ;;
    -p) PORT="$2"; shift 2 ;;
    -U) USER="$2"; shift 2 ;;
    -s) SCHEMA="$2"; shift 2 ;;
    -o) OUTDIR="$2"; shift 2 ;;
    -b) BATCH="$2"; shift 2 ;;
    --opendata) OPENDATA=true; shift ;;
    *)  echo "Usage: $0 {backup|restore|list} -d DB -h HOST -p PORT -U USER [-s SCHEMA] [-o OUTDIR] [-b BATCH] [--opendata]"; exit 1 ;;
  esac
done

[ -z "$DB" ] && { echo "ERROR: -d DB required"; exit 1; }
[ -z "$MODE" ] && { echo "ERROR: specify backup|restore|list"; exit 1; }

PG_OPTS="-h $HOST -p $PORT -U $USER -d $DB"

# Include OPENDATA tables when flag is set
if [ "$OPENDATA" = true ]; then
  ALL_TABLES="${TABLES_OPENDATA}" ${TABLES_L0} ${TABLES_L1} ${TABLES_L2} ${TABLES_L3} ${TABLES_L4} 
  RESTORE_ORDER="${TABLES_OPENDATA}" ${TABLES_L0} ${TABLES_L1} ${TABLES_L2} ${TABLES_L3} ${TABLES_L4} 
fi

# ─── List tables in topological order ──────────────────────────────────────
list_tables() {
  echo "=== Tables in topological order (restore sequence) ==="
  i=0
  for tbl in $RESTORE_ORDER; do
    i=$((i + 1))
    echo "  [$i] $tbl"
  done
  echo ""
  echo "Total: $i tables"
}

# ─── Backup: one file per table, gzipped ───────────────────────────────────
do_backup() {
  mkdir -p "$OUTDIR"
  echo "Backup → $OUTDIR/"

  for tbl in $ALL_TABLES; do
    fqtn="${SCHEMA}.${tbl}"
    outfile="$OUTDIR/${tbl}.sql"

    echo "  Exporting: $tbl ..."
    pg_dump $PG_OPTS \
      --schema="$SCHEMA" \
      --table="$fqtn" \
      --data-only \
      --column-inserts \
      --rows-per-insert="${BATCH}" \
      --no-owner \
      --no-acl \
      --no-comments  > "$outfile"

    gzip -f "$outfile"
    echo "    → ${outfile}.gz ($(stat -c%s "${outfile}.gz" 2>/dev/null || stat -f%z "${outfile}.gz" 2>/dev/null) bytes)"
  done

  # Export schema (structure only) for full restore
  echo "  Exporting schema ..."
  pg_dump $PG_OPTS \
    --schema="$SCHEMA" \
    --schema-only \
    --no-owner \
    --no-acl > "$OUTDIR/00_schema.sql"
  gzip -f "$OUTDIR/00_schema.sql"
  echo "  Done: $(ls -1 ${OUTDIR}/*.sql.gz 2>/dev/null | wc -l) files"
}

# ─── Restore: schema first, then data in topological order ─────────────────
do_restore() {
  echo "Restore from $OUTDIR/"

  # Restore schema
  schema_file="$OUTDIR/00_schema.sql.gz"
  if [ -f "$schema_file" ]; then
    echo "  Restoring schema ..."
    gunzip -c "$schema_file" | psql $PG_OPTS -v ON_ERROR_STOP=1
  else
    echo "  WARNING: $schema_file not found, skipping schema restore"
  fi

  # Restore data in topological order
  echo "  Restoring data (FK order) ..."
  for tbl in $RESTORE_ORDER; do
    datafile="$OUTDIR/${tbl}.sql.gz"
    if [ -f "$datafile" ]; then
      echo "  Importing: $tbl ..."
      gunzip -c "$datafile" | psql $PG_OPTS -v ON_ERROR_STOP=1
    else
      echo "  SKIP: $tbl (file not found)"
    fi
  done
  echo "  Restore complete"
}

# ─── Main ──────────────────────────────────────────────────────────────────
case "$MODE" in
  list)    list_tables ;;
  backup)  do_backup ;;
  restore) do_restore ;;
esac
