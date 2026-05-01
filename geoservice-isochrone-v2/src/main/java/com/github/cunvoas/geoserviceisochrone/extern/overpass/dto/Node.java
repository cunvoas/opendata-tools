package com.github.cunvoas.geoserviceisochrone.extern.overpass.dto;

import java.util.Map;

/**
 * Représente un nœud OSM dans la réponse Overpass JSON.
 * <p>
 * Exemple JSON :
 * <pre>
 *   {
 *     "type": "node",
 *     "id": 123,
 *     "lat": 49.0,
 *     "lon": 1.2,
 *     "tags": { "amenity": "school" }
 *   }
 * </pre>
 */
public class Node extends Element {
    public double lat;
    public double lon;
    public Map<String, String> tags;
}