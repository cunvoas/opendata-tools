package com.github.cunvoas.geoserviceisochrone.service.park;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;

import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisShape;

/**
 * DTO to factorise compute method.
 */
public class ComputeIrisDto {
	
	/**
	 * Constructor.
	 */
	public ComputeIrisDto() {
		super();
	}
	
	/**
	 * Constructor.
	 * @param irisShape shape
	 */
	public ComputeIrisDto(IrisShape irisShape) {
		super();
		this.polygonParkAreas=irisShape.getContour();
		this.polygonParkAreasOms=irisShape.getContour();
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

	// to compute people for 0.5ha at 300m
	Geometry polygonParkAreasSustainableOms = null;
	
	BigDecimal popWithSufficient = BigDecimal.ZERO;
	Boolean withSufficient = Boolean.FALSE;
}
