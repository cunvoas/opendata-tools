package com.github.cunvoas.geoserviceisochrone.extern.gouv.adress.dto;

import com.github.cunvoas.geoserviceisochrone.model.geojson.GeoJsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO.
 */
@Data
@EqualsAndHashCode(callSuper=false, onlyExplicitlyIncluded = true)
public class DtoGeoJsonAdress extends GeoJsonProperty {
	
	@EqualsAndHashCode.Include
	private String id; //80021_6590_00008",
    private String label;//":"8 Boulevard du Port 80000 Amiens",
    private Float score; //":0.49159121588068583,
    private String housenumber; //":"8",
    private String type;//":"housenumber",
    private String name;//":"8 Boulevard du Port",
	private String postcode;//":"80000",
	private String citycode;//":"80021",
    private String x;//":648952.58,
    private String y;//":6977867.25,
    private String city;//":"Amiens",
    private String context;//":"80, Somme, Hauts-de-France",
    private Float importance;//":0.6706612694243868,
    private String street;//":"Boulevard du Port"
    
	

}
