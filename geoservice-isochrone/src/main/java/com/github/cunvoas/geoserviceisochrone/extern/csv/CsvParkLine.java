package com.github.cunvoas.geoserviceisochrone.extern.csv;

import lombok.Data;

@Data
public class CsvParkLine {

	private String reserved1;
	private String reserved2;
	private String city;
	private String block;
	private String park;
	private String entrance;
	private String url;
	
	
	public String toString() {
		return String.format("city: %s, block:%s, park%s, entrance: %s", city, block, park, entrance);
	}
}
