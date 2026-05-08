package com.github.cunvoas.geoserviceisochrone.model.geojson;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * DTO représentant la racine d'un document GeoJson (FeatureCollection).
 * Conforme à la spécification GeoJson RFC-7946.
 * Contient la liste des features du document.
 * @see https://www.rfc-editor.org/rfc/rfc7946
 * @see https://geojson.org/geojson-spec.html
 */
@Data
public class GeoJsonRoot {
    /** Type de la racine GeoJson (toujours "FeatureCollection"). */
    private final String type="FeatureCollection";
    /** Liste des features contenues dans la collection. */
    private List<GeoJsonFeature> features = new ArrayList<>();
}