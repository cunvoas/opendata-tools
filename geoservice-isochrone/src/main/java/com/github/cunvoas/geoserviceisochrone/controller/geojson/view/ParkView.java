package com.github.cunvoas.geoserviceisochrone.controller.geojson.view;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ParkView extends GeoJsonProperty {
	@EqualsAndHashCode.Include
	private String id;
	private String quartier;
	private String name;
	//private String desc;
	// valid with OMS definition
	private Boolean oms;
	private Boolean dense;
	private String fillColor="#ede7e6";
	private String areaPerPeople;
	private String area;
	private String people;
	
	
}
