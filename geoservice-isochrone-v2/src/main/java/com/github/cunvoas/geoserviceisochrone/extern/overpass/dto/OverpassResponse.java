package com.github.cunvoas.geoserviceisochrone.extern.overpass.dto;

import java.util.List;

/**
 * Représente la racine d'une réponse Overpass JSON.
 * <p>
 * Exemple JSON :
 * <pre>
 *   {
 *     "elements": [ ... ]
 *   }
 * </pre>
 */
public class OverpassResponse {
    public List<Element> elements;

    /**
     * Bornes géographiques de la réponse (optionnel).
     */
    public Bounds bounds;
}