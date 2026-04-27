package com.github.cunvoas.geoserviceisochrone.extern.csv;

import lombok.Data;

/**
 * Objet de transfert (DTO) représentant une ligne d'entrée de parc.
 * Contient les informations d'une entrée de parc lue depuis un fichier CSV.
 */
@Data
public class CsvParkLine {


	private String reserved1;
	private String reserved2;
	private String city;
	private String block;
	private String park;
	private String entrance;
	private String url;
	
	
	/**
	 * Retourne une représentation textuelle de l'objet, utile pour le débogage.
	 */
	public String toString() {
		return String.format("city: %s, block:%s, park%s, entrance: %s", city, block, park, entrance);
	}
}