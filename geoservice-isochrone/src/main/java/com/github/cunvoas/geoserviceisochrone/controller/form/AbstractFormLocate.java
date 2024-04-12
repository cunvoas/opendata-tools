package com.github.cunvoas.geoserviceisochrone.controller.form;

import java.util.List;

import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;

import lombok.Data;

@Data
public abstract class AbstractFormLocate {

	protected Long idRegion;
	protected Long idCommunauteDeCommunes;
	protected Long idCommune;

	protected List<Region> regions;
	protected List<CommunauteCommune> communautesDeCommunes;
	protected List<City> communes;
	
	private String nameRegion;
	private String nameCommunauteDeCommunes;
	private String nameCommune;
	
	// FIXME process with User Preferences
	public void autoLocate() {
		this.setIdRegion(9L);
		this.setIdCommunauteDeCommunes(1L);
	}
}
