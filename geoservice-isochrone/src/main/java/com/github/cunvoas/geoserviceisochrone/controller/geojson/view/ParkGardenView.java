package com.github.cunvoas.geoserviceisochrone.controller.geojson.view;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false, onlyExplicitlyIncluded = true)
public class ParkGardenView extends GeoJsonProperty {
	@EqualsAndHashCode.Include
	private String id;
	private String name;
	private String source;
	private Double surface;
	
	
}
