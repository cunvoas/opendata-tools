# gpkg2geojson

Convertit des fichiers GeoPackage (GPKG) en GeoJSON Lines (un objet Feature GeoJSON par ligne), reprojetés en WGS 84 (EPSG:4326).

## Installation

```bash
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

## Usage

```bash
# tous les *.gpkg du dossier courant
python gpkg2geojson.py

# ou des fichiers précis
python gpkg2geojson.py fichier1.gpkg [fichier2.gpkg ...]
```

Pour chaque GPKG passé en argument, le script écrit un fichier de même nom avec l'extension `.geojsonl`, à côté de l'original :

```
PJ_59_2018.gpkg  ->  PJ_59_2018.geojsonl
PJ_27_2022.gpkg  ->  PJ_27_2022.geojsonl
```

Les fichiers `.gpkg` d'origine ne sont pas modifiés.

## Limites

- Seule la première couche de chaque GPKG est convertie.
- Les colonnes de type date/heure ne sont pas sérialisables en JSON : le script échouera dessus.
- Le CRS source doit être défini dans le GPKG. Sinon, il faut ajouter `set_crs` dans le script.

## Chargement en masse

Le format une-entité-par-ligne se charge ligne à ligne (ex. `COPY`, `jq -c`, ou import par lot).
