package com.github.cunvoas.geoserviceisochrone.extern.overpass.geojsondto;

import java.util.List;

/**
 * Représente la racine d'une réponse Overpass au format GeoJSON (FeatureCollection).
 * <p>
 * Exemple JSON :
 * <pre>
 *   {
 *     "type": "FeatureCollection",
 *     "generator": "overpass-turbo",
 *     "copyright": "...",
 *     "timestamp": "...",
 *     "features": [ ... ]
 *   }
 * </pre>
 * <ul>
 *   <li>type : toujours "FeatureCollection"</li>
 *   <li>generator : nom du générateur (ex : overpass-turbo)</li>
 *   <li>copyright : mention légale OSM</li>
 *   <li>timestamp : date d'export</li>
 *   <li>features : liste des entités géographiques (parcs, etc.)</li>
 * </ul>
 */
public class OverpassGeojsonResponse {
    public String type; // FeatureCollection
    public String generator;
    public String copyright;
    public String timestamp;
    public List<DtoGeojsonFeature> features;
}