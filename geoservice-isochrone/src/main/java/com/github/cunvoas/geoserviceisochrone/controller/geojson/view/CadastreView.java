package com.github.cunvoas.geoserviceisochrone.controller.geojson.view;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonProperty;

import lombok.Data;

@Data
public class CadastreView extends GeoJsonProperty {

	private String idInsee;
	private String nom;
	
}
