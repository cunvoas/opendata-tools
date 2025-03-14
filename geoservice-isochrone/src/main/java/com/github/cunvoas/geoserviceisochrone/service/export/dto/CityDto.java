package com.github.cunvoas.geoserviceisochrone.service.export.dto;

import com.github.cunvoas.geoserviceisochrone.model.opendata.City;

import lombok.Data;

/**
 * DTO.
 */
@Data
public class CityDto {
	
	/**
	 * Constructor
	 * @param model BO
	 */
	public CityDto(City model) {
		super();
		this.id=model.getId();
		this.name=model.getName();
		this.postalCode=model.getPostalCode();
		this.inseeCode=model.getInseeCode();
		if (model.getCoordinate()!=null) {
			this.lonX=model.getCoordinate().getX();
			this.latY=model.getCoordinate().getY();
		}
	}
	
	private Long id;
	private String name;
	private String postalCode;
	private String inseeCode;
	private Double lonX;
	private Double latY;

}
