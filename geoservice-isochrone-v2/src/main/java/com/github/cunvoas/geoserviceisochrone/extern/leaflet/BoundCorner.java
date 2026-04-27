package com.github.cunvoas.geoserviceisochrone.extern.leaflet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

/**
 * Représente un coin d'une zone géographique, défini par une latitude et une longitude.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "lat", "lng" })
@Data
public class BoundCorner {

	/**
	 * Latitude du coin.
	 */
	@JsonProperty("lat")
	public Double lat;
	/**
	 * Longitude du coin.
	 */
	@JsonProperty("lng")
	public Double lng;

}