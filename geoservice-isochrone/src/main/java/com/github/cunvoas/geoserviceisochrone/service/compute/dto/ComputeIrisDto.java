package com.github.cunvoas.geoserviceisochrone.service.compute.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;

import com.github.cunvoas.geoserviceisochrone.model.opendata.IrisShape;
import com.github.cunvoas.geoserviceisochrone.service.park.ComputeResultDto;

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
	public Integer annee=Integer.MIN_VALUE;
	
	public Boolean isDense = Boolean.TRUE;
	public List<String> parcNames = new ArrayList<>();
	public String parcName = "";
	public BigDecimal popAll = BigDecimal.ZERO;
	public Boolean allAreOms = Boolean.FALSE;
	
	public Geometry polygonParkAreas = null;
	public ComputeResultDto result=new ComputeResultDto();
	
	public Geometry polygonParkAreasOms = null;
	public ComputeResultDto resultOms=new ComputeResultDto();

	// to compute people for 0.5ha at 300m
	public Geometry polygonParkAreasSustainableOms = null;
	
	public BigDecimal popWithSufficient = BigDecimal.ZERO;
	public Boolean withSufficient = Boolean.FALSE;
}
