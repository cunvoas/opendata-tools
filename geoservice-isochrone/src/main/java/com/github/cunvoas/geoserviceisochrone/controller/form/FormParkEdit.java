package com.github.cunvoas.geoserviceisochrone.controller.form;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcPrefecture;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;

import lombok.Data;

@Data
public class FormParkEdit {

	private Long idRegion;
	private Long idCommunauteDeCommunes; 
	private Long idCommune; 
	private Long idPark;//parcEtJardin
	private String nameCommune;

	// parcs preselection
	private List<Region> regions;
	private List<CommunauteCommune> communautesDeCommunes;
	private List<City> communes;
	
	// data from DB
	private ParcEtJardin parcEtJardin;
	private ParcPrefecture parcPrefecture;
	private ParkArea parkArea;
	private List<ParkEntrance> parkEntrances;
	private ParkAreaComputed parkAreaComputed;
	
	private String nomParc;
	private Long surface;
	private Boolean computeNeeded=Boolean.FALSE;
	
	
	// park photo
	private MultipartFile photo;
	
	//edit fields
	//parkArea
	private String description;
	private Long idParkType;
	
	//parkAreaCompute
	private Boolean oms;
	//parkPrefecture
	private Long idParkAndGarden;//parcEtJardin
	
}
