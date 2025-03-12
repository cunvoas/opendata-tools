package com.github.cunvoas.geoserviceisochrone.extern.leaflet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

/**
 * DTO.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "lat", "lng" })
@Data
public class BoundCorner {

	@JsonProperty("lat")
	public Double lat;
	@JsonProperty("lng")
	public Double lng;

}
