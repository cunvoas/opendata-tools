package com.github.cunvoas.geoserviceisochrone.extern.overpass.rawdto;

import java.util.List;
import java.util.Map;

import com.github.cunvoas.geoserviceisochrone.extern.overpass.common.LatLon;

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