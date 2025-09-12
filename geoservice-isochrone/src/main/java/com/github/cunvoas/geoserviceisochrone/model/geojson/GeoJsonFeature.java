package com.github.cunvoas.geoserviceisochrone.model.geojson;

import org.locationtech.jts.geom.Geometry;

import com.bedatadriven.jackson.datatype.jts.serialization.GeometryDeserializer;
import com.bedatadriven.jackson.datatype.jts.serialization.GeometrySerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

/**
 * DTO représentant une entité GeoJson de type Feature.
 * Conforme à la spécification GeoJson RFC-7946.
 * Contient la géométrie et les propriétés associées à la feature.
 */
@Data
public class GeoJsonFeature {
    /** Type de l'entité GeoJson (toujours "Feature"). */
    private final String type="Feature";
    /** Géométrie de la feature (Point, LineString, Polygon, etc.). */
    @JsonSerialize(using = GeometrySerializer.class)
    @JsonDeserialize(contentUsing = GeometryDeserializer.class)
    private Geometry geometry;
    /** Propriétés associées à la feature. */
    private GeoJsonProperty properties;
	
	@Override
	public boolean equals(Object obj) {
		return properties.equals(obj);
	}
	
	@Override
	public int hashCode() {
		return properties.hashCode();
	}
	
}