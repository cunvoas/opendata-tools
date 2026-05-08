package com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.dto;

import java.util.ArrayList;
import java.util.List;

public class DtoGeometry {
	private String type;
	private List<DtoCoordinate> coordinates = new ArrayList<>();
	private List<DtoConstraint> constraints = new ArrayList<>();
	
	public String getType() {
		return type;
	}
	public List<DtoConstraint> getConstraints() {
		return constraints;
	}
	public void setConstraints(List<DtoConstraint> constraints) {
		this.constraints = constraints;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<DtoCoordinate> getCoordinates() {
		return coordinates;
	}
	public void setCoordinates(List<DtoCoordinate> coordinate) {
		this.coordinates = coordinate;
	}



}
