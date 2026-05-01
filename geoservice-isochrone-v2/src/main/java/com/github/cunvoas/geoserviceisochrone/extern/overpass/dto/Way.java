package com.github.cunvoas.geoserviceisochrone.extern.overpass.dto;

import java.util.List;
import java.util.Map;

/**
 * Représente un chemin OSM (Way) dans la réponse Overpass JSON.
 * <p>
 * Exemple JSON :
 * <pre>
 *   {
 *     "type": "way",
 *     "id": 456,
 *     "nodes": [123, 124, 125],
 *     "tags": { "highway": "residential" }
 *   }
 * </pre>
 */
public class Way extends Element {
    public List<Long> nodes;
    public Map<String, String> tags;
    
    /**
     * Liste des points géométriques (présent pour les ways et relations, parfois nodes).
     */
    public List<LatLon> geometry;
}