package com.github.cunvoas.geoserviceisochrone.controller.form;

import java.util.List;

import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;

import lombok.Data;

@Data
public class FormParkList {

	private Long idRegion;
	private Long idCommunauteDeCommunes;
	private Long idCommune;
	private Long idParkType;
	private String parkCase="all";

	private String nameRegion;
	private String nameCommunauteDeCommunes;
	private String nameCommune;

	private List<Region> regions;
	private List<CommunauteCommune> communautesDeCommunes;
	private List<City> communes;
	
	
	private Integer page=1;
	private Integer size=300;
}
