package com.github.cunvoas.geoserviceisochrone.extern.csv;

import lombok.Data;

/**
 * Objet de transfert (DTO) pour la mise à jour des informations d'un parc.
 * Contient les champs nécessaires à la modification des données d'un parc et de ses entrées.
 */
@Data
public class CsvParkUpdate {

	private String cityId;
	private String parkId;
	private String nom;
	private String surface;
	private String nomE;
	private String coord;
	
	
	/**
	 * Retourne une représentation textuelle de l'objet, utile pour le débogage.
	 */
	public String toString() {
		return String.format("cityId: %s, parkId:%s, nom%s, surface: %s, nomE: %s, coord: %s", cityId, nom, parkId, surface, nomE, coord);
	}
}