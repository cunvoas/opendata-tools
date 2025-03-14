package com.github.cunvoas.geoserviceisochrone.service.export.dto;

import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;

import lombok.Data;

/**
 * DTO.
 */
@Data
public class RegionDto {

	/**
	 * Constructor
	 * @param model BO
	 */
	public RegionDto(Region model) {
		super();
		this.id=model.getId();
		this.name=model.getName();
	}
	
	private Long id;
	private String name;

}
