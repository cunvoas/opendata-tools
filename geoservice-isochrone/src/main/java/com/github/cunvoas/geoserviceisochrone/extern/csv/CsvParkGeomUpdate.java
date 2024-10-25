package com.github.cunvoas.geoserviceisochrone.extern.csv;

import lombok.Data;

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
	
	public String toString() {
		return String.format("nom: %s, surface: %s, geom: %s", nom, surface, geom);
	}
}
