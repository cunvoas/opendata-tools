package com.github.cunvoas.geoserviceisochrone.model.geojson;

import lombok.Data;

/**
 * Classe abstraite représentant les propriétés d'une entité GeoJson.
 * À spécialiser selon le type de données métier à embarquer dans la propriété.
 * Conforme à la spécification GeoJson RFC-7946.
 */
@Data
public abstract class GeoJsonProperty {
    /**
     * Teste l'égalité entre deux propriétés GeoJson.
     * @param obj objet à comparer
     * @return true si égal, false sinon
     */
    @Override
    public abstract boolean equals(Object obj);
    /**
     * Calcule le hashcode de la propriété GeoJson.
     * @return hashcode
     */
    @Override
    public abstract int hashCode();
}