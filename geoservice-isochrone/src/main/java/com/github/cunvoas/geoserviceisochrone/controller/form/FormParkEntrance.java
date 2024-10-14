package com.github.cunvoas.geoserviceisochrone.controller.form;


import java.util.List;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;

import lombok.Data;

@Data
public class FormParkEntrance extends AbstractFormLocate {
	
	public FormParkEntrance() {
		super();
	}
	public FormParkEntrance(FormParkEntranceDetail detail) {
		super();
		super.idRegion=detail.getIdRegion();
		super.idCommunauteDeCommunes=detail.getIdCommunauteDeCommunes();
		super.idCommune=detail.getIdCommune();
		this.idPark=detail.getIdPark();
		this.areaId=detail.getAreaId();
		this.idEntrance=detail.getIdEntrance();
		
	}

	// parcs preselection
	private List<ParcEtJardin> parks;
	private List<ParkEntrance> parkEntrances;
	
	// select id
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
