package com.github.cunvoas.geoserviceisochrone.controller.geojson.view;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for geojson Cadastre.
 */
@Data
@EqualsAndHashCode(callSuper=false, onlyExplicitlyIncluded = true)
public class CadastreView extends GeoJsonProperty {

	@EqualsAndHashCode.Include
	private String idInsee;
	
	private String nom;
	private String communauteCommune;
		
}
