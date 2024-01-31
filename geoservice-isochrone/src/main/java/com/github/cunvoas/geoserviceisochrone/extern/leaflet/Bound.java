package com.github.cunvoas.geoserviceisochrone.extern.leaflet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

/**
 * @author cus
 * sample: { "_southWest": { "lat": 50.60677419392376, "lng": 3.0161762237548833 }, "_northEast": { "lat": 50.6503312283444, "lng": 3.173933029174805 } }}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "_southWest", "_northEast" })
@Data
public class Bound {

	@JsonProperty("_southWest")
	public BoundCorner southWest;
	@JsonProperty("_northEast")
	public BoundCorner northEast;
}