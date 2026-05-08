package com.github.cunvoas.geoserviceisochrone.extern.csv;

import lombok.Data;

/**
 * Objet de transfert (DTO) pour la mise à jour de la géométrie des parcs.
 * Contient les informations nécessaires à la modification des contours des parcs.
 */
@Data
public class CsvParkGeomUpdate {
	
	//objectid,id,nom,quartier,nom_liste,adresse,surface,geom
	
	private String objectid;
	private String id;
	private String nom;
	private String quartier;
	private String nomListe;
	private String adresse;
	private String surface;
	private String geom;
	
	/**
	 * Retourne une représentation textuelle de l'objet, utile pour le débogage.
	 */
	public String toString() {
		return String.format("nom: %s, surface: %s, geom: %s", nom, surface, geom);
	}
}