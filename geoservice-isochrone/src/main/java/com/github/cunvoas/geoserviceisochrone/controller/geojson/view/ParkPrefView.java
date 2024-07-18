package com.github.cunvoas.geoserviceisochrone.controller.geojson.view;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ParkPrefView extends GeoJsonProperty {
	@EqualsAndHashCode.Include
	private String id;
	private String name;
	private String namePrefecture;
	private Long idCommune;
	private Long surface;
	private Boolean processed=Boolean.FALSE;
	private String status;
	private String source;
	
	private String fillColor="#2f23a4";
	

	private Long idParcJardin;
	private String nameParcJardin;
	private String quartier;
	private String type;
	private String sousType;
	
}
