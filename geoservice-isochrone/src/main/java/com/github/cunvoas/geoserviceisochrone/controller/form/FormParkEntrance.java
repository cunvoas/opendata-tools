package com.github.cunvoas.geoserviceisochrone.controller.form;


import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;

import lombok.Data;

@Data
public class FormParkEntrance {
	
	public FormParkEntrance() {
		super();
	}
	public FormParkEntrance(FormParkEntranceDetail detail) {
		super();
		this.idRegion=detail.getIdRegion();
		this.idCommunauteDeCommunes=detail.getIdCommunauteDeCommunes();
		this.idCommune=detail.getIdCommune();
		this.idPark=detail.getIdPark();
		this.areaId=detail.getAreaId();
		this.idEntrance=detail.getIdEntrance();
		
	}

	// parcs preselection
	private List<Region> regions;
	private List<CommunauteCommune> communautesDeCommunes;
	private List<City> communes;
	private List<ParcEtJardin> parks;
	private List<ParkEntrance> parkEntrances;
	
	// select id
	private Long idRegion;
	private Long idCommunauteDeCommunes;
	private Long idCommune;
	private String nameCommune;
	private Long idPark; //ParcEtJardin
	private String namePark;
	private Long idEntrance;
	private String mapLat;
	private String mapLng;
	private Long areaId; //ParcArea
	
	
	// worked object
	private ParcEtJardin parcEtJardin;
	private ParkEntrance parkEntrance;
	private FormParkEntranceDetail parkEntranceDetail;
	private ParkArea parkArea;
	

	
	

	
}