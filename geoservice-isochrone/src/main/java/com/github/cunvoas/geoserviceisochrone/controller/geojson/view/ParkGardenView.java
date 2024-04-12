package com.github.cunvoas.geoserviceisochrone.controller.geojson.view;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonProperty;

import lombok.Data;

@Data
public class ParkGardenView extends GeoJsonProperty {

	private String id;
	private String name;
	private String source;
	private Double surface;
	
	
}
