package com.github.cunvoas.geoserviceisochrone.service.export.dto;

import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;

import lombok.Data;

/**
 * DTO représentant une région pour l'export.
 * Permet de transférer les informations essentielles d'une région (nom, identifiant).
 */
@Data
public class RegionDto {

	/**
	 * Construit un DTO à partir d'un objet métier Region.
	 *
	 * @param model la région source
	 */
	public RegionDto(Region model) {
		super();
		this.id=model.getId();
		this.name=model.getName();
	}
	
	/** Identifiant de la région */
	private Long id;
	/** Nom de la région */
	private String name;

}