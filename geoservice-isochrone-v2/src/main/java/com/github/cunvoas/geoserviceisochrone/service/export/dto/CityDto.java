package com.github.cunvoas.geoserviceisochrone.service.export.dto;

import com.github.cunvoas.geoserviceisochrone.model.opendata.City;

import lombok.Data;

/**
 * DTO représentant une commune pour l'export.
 * Permet de transférer les informations essentielles d'une commune (nom, code INSEE, coordonnées, etc.).
 */
@Data
public class CityDto {
	
	/**
	 * Construit un DTO à partir d'un objet métier City.
	 *
	 * @param model la commune source
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
	
	/** Identifiant de la commune */
	private Long id;
	/** Nom de la commune */
	private String name;
	/** Code postal */
	private String postalCode;
	/** Code INSEE */
	private String inseeCode;
	/** Longitude (X) */
	private Double lonX;
	/** Latitude (Y) */
	private Double latY;

}