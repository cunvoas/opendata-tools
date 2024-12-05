package com.github.cunvoas.geoserviceisochrone.service.park;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;

import com.github.cunvoas.geoserviceisochrone.model.opendata.InseeCarre200mOnlyShape;

/**
 * DTO to factorise compute method.
 */
public class ComputeDto {
	
	public ComputeDto() {
		super();
	}
	
	public ComputeDto(InseeCarre200mOnlyShape carreShape) {
		super();
		this.polygonParkAreas=carreShape.getGeoShape();
		this.polygonParkAreasOms=carreShape.getGeoShape();
	}
	Integer annee=Integer.MIN_VALUE;
	
	Boolean isDense = Boolean.TRUE;
	List<String> parcNames = new ArrayList<>();
	String parcName = "";
	BigDecimal popAll = BigDecimal.ZERO;
	Boolean allAreOms = Boolean.FALSE;
	
	Geometry polygonParkAreas = null;
	ComputeResultDto result=new ComputeResultDto();
	
	Geometry polygonParkAreasOms = null;
	ComputeResultDto resultOms=new ComputeResultDto();
	
}
