package com.github.cunvoas.geoserviceisochrone.extern.leaflet;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

/**
 * Représente une zone géographique rectangulaire définie par deux coins (sud-ouest et nord-est).
 * Utile pour décrire des bornes sur une carte Leaflet.
 * Exemple d'utilisation :
 * { "_southWest": { "lat": 50.60677419392376, "lng": 3.0161762237548833 }, "_northEast": { "lat": 50.6503312283444, "lng": 3.173933029174805 } }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "_southWest", "_northEast" })
@Data
public class Bound {

	/**
	 * Coin sud-ouest de la zone (latitude/longitude minimale).
	 */
	@JsonProperty("_southWest")
	public BoundCorner southWest;
	/**
	 * Coin nord-est de la zone (latitude/longitude maximale).
	 */
	@JsonProperty("_northEast")
	public BoundCorner northEast;
}