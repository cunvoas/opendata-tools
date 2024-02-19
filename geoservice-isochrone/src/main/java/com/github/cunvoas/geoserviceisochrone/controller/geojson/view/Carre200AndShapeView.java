package com.github.cunvoas.geoserviceisochrone.controller.geojson.view;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonProperty;

import lombok.Data;


@Data
public class Carre200AndShapeView extends GeoJsonProperty {
	private String id;
	private String idInspire;
	private String commune;

	private String people;
	private String popParkIncluded;
	private String popParkExcluded;
	
	private String fillColor="#060512";
	private String areaPerPeople;
	private String area;
	
}
