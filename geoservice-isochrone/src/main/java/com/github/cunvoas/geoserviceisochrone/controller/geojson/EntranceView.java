package com.github.cunvoas.geoserviceisochrone.controller.geojson;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonProperty;

import lombok.Data;

@Data
public class EntranceView extends GeoJsonProperty {

	private String id;
	private String name;
	private String fillColor="#ede7e6";
	
}
