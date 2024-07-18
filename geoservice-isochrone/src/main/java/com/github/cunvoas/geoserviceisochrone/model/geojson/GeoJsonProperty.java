package com.github.cunvoas.geoserviceisochrone.model.geojson;

import lombok.Data;

@Data
public abstract class GeoJsonProperty {
	
	@Override
	public abstract boolean equals(Object obj);
	
	@Override
	public abstract int hashCode();
	

}
