package com.github.cunvoas.geoserviceisochrone.model;

import org.locationtech.jts.geom.Point;

import lombok.Data;

/**
 * @author cus
 *
 */
@Data
public class Coordinate {
	private Double longitude; //x
	private Double latitude;  //y
	
	public Coordinate(Double longitude, Double latitude) {
		super();
		this.longitude=longitude;
		this.latitude=latitude;
	}
	
	public Double getX() {
		return longitude;
	}
	public void setX(Double longitude) {
		this.longitude = longitude;
	}
	public Double getY() {
		return latitude;
	}
	public void setY(Double latitude) {
		this.latitude = latitude;
	}

	@Override
	public String toString() {
		return longitude +","+ latitude;
	}
	
	

}
