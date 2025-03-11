package com.github.cunvoas.geoserviceisochrone.model.geojson;

import org.locationtech.jts.geom.Geometry;

import com.bedatadriven.jackson.datatype.jts.serialization.GeometryDeserializer;
import com.bedatadriven.jackson.datatype.jts.serialization.GeometrySerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

/**
 * DTO for GeoJson feature.
 * Implements GeoJson RFC-7946.
 * @author cunvoas
 */
@Data
public class GeoJsonFeature {
	
	private final String type="Feature";
	
    @JsonSerialize(using = GeometrySerializer.class)
    @JsonDeserialize(contentUsing = GeometryDeserializer.class)
	private Geometry geometry;
    
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
