import json
import sys
from pathlib import Path

import geopandas as gpd


def convert(gpkg_path: Path) -> Path:
    gdf = gpd.read_file(gpkg_path).to_crs(epsg=4326)
    out = gpkg_path.with_suffix(".geojsonl")
    with out.open("w", encoding="utf-8") as f:
        for feature in gdf.iterfeatures(na="null"):
            f.write(json.dumps(feature, ensure_ascii=False) + "\n")
    return out


if __name__ == "__main__":
    paths = [Path(arg) for arg in sys.argv[1:]] or sorted(Path.cwd().glob("*.gpkg"))
    for path in paths:
        print(convert(path))
