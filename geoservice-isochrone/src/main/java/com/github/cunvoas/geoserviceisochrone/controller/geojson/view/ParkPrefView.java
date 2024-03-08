package com.github.cunvoas.geoserviceisochrone.controller.geojson.view;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonProperty;

import lombok.Data;

@Data
public class ParkPrefView extends GeoJsonProperty {

	private String id;
	private String name;
	private String namePrefecture;
	private Long idParcJardin;
	private String nameParcJardin;
	private Long surface;
	private Boolean processed=Boolean.FALSE;
	
	private String fillColor="#2f23a4";
	
}
