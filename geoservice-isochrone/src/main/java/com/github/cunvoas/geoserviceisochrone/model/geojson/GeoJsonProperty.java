package com.github.cunvoas.geoserviceisochrone.model.geojson;

import lombok.Data;

/**
 * DTO for GeoJson property.
 * Implements GeoJson RFC-7946.
 */
@Data
public abstract class GeoJsonProperty {
	
	@Override
	public abstract boolean equals(Object obj);
	
	@Override
	public abstract int hashCode();
	

}
