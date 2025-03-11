package com.github.cunvoas.geoserviceisochrone.extern.csv;

import java.util.ArrayList;
import java.util.List;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;

import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * DTO.
 */
@Data
@NoArgsConstructor
public class CsvMassUpdatePivot {
	
	private Region region;
	private CommunauteCommune comm2co;
	private City commune;
	
	private ParcEtJardin parcEtJardin;
	private List<ParkEntrance> entrances = new ArrayList<>();

}
