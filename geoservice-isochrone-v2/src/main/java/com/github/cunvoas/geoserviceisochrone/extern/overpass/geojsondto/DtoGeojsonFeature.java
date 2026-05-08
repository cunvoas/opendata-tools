package com.github.cunvoas.geoserviceisochrone.extern.overpass.geojsondto;

import java.util.Map;
import java.util.List;

/**
 * Représente une entité GeoJSON de type Feature issue d'un export Overpass.
 * <p>
 * Exemple JSON :
 * <pre>
 *   {
 *     "type": "Feature",
 *     "properties": {
 *       "@id": "relation/3428166",
 *       "leisure": "park",
 *       "name": "Parc de la Deule",
 *       ...
 *     },
 *     "geometry": {
 *       "type": "Polygon",
 *       "coordinates": [ ... ]
 *     }
 *   }
 * </pre>
 * <ul>
 *   <li>type : toujours "Feature"</li>
 *   <li>properties : attributs OSM (id, nom, type, etc.)</li>
 *   <li>geometry : géométrie GeoJSON (Polygon, MultiPolygon, ...)</li>
 * </ul>
 */
public class DtoGeojsonFeature {
    /** Type GeoJSON, toujours "Feature". */
    public String type;
    /** Propriétés OSM de la feature (id, nom, type, etc.). */
    public Map<String, Object> properties;
    /** Géométrie GeoJSON de la feature. */
    public Geometry geometry;

    /**
     * Représente la géométrie d'une feature GeoJSON.
     * <p>
     * Exemple :
     * <pre>
     *   {
     *     "type": "Polygon",
     *     "coordinates": [ ... ]
     *   }
     * </pre>
     * <ul>
     *   <li>type : "Polygon", "MultiPolygon", etc.</li>
     *   <li>coordinates : liste de coordonnées imbriquées selon le type</li>
     * </ul>
     */
    public static class Geometry {
        /** Type de géométrie GeoJSON (Polygon, MultiPolygon, etc.). */
        public String type;
        
        /** Coordonnées de la géométrie, structure imbriquée selon le type.
         * <p>
         * Pour un Polygon, il s'agit d'une liste de listes de points [ [ [lon, lat], ... ] ]
         * Pour un MultiPolygon, il s'agit d'une liste de listes de listes de points [ [ [ [lon, lat], ... ] ], ... ]
         * Chaque point est un tableau de deux doubles : [longitude, latitude].
         */
        public List<Object> coordinates;
    }
}