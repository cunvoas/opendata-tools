package com.github.cunvoas.geoserviceisochrone.extern.csv;

import lombok.Data;

@Data
public class CsvParkUpdate {

	private String cityId;
	private String parkId;
	private String nom;
	private String surface;
	private String nomE;
	private String coord;
	
	
	public String toString() {
		return String.format("cityId: %s, parkId:%s, nom%s, surface: %s, nomE: %s, coord: %s", cityId, nom, parkId, surface, nomE, coord);
	}
}
