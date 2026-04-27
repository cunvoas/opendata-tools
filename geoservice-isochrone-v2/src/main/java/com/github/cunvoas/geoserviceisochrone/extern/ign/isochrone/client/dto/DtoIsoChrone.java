package com.github.cunvoas.geoserviceisochrone.extern.ign.isochrone.client.dto;

import java.util.ArrayList;
import java.util.List;

public class DtoIsoChrone {
	 private String point;
	 private String resource;
	 private String resourceVersion;
	 private String costType;
	 private Integer costValue;
	 private String timeUnit;
	 private String profile;
	 private String direction;
	 private String crs;
	 private DtoGeometry geometry = new DtoGeometry();
	 
	 private List < DtoConstraint> constraints = new ArrayList < > ();

	public String getPoint() {
		return point;
	}

	public void setPoint(String point) {
		this.point = point;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}

	public String getResourceVersion() {
		return resourceVersion;
	}

	public void setResourceVersion(String resourceVersion) {
		this.resourceVersion = resourceVersion;
	}

	public String getCostType() {
		return costType;
	}

	public void setCostType(String costType) {
		this.costType = costType;
	}

	public Integer getCostValue() {
		return costValue;
	}

	public void setCostValue(Integer costValue) {
		this.costValue = costValue;
	}

	public String getTimeUnit() {
		return timeUnit;
	}

	public void setTimeUnit(String timeUnit) {
		this.timeUnit = timeUnit;
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getCrs() {
		return crs;
	}

	public void setCrs(String crs) {
		this.crs = crs;
	}

	public DtoGeometry getGeometry() {
		return geometry;
	}

	public void setGeometry(DtoGeometry geometry) {
		this.geometry = geometry;
	}

	public List<DtoConstraint> getConstraints() {
		return constraints;
	}

	public void setConstraints(List<DtoConstraint> constraints) {
		this.constraints = constraints;
	}
	 
	 
	 


}
