package com.github.cunvoas.geoserviceisochrone.controller.geojson;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonProperty;

import lombok.Data;

@Data
public class ParkView extends GeoJsonProperty {

	private String id;
	private String quartier;
	private String name;
	//private String desc;
	// valid with OMS definition
	private Boolean oms;
	private String fillColor="#ede7e6";
	private String areaPerPeople;
	private String area;
	private String people;
	
	
}
