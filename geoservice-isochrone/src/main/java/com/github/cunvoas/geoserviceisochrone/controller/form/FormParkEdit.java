package com.github.cunvoas.geoserviceisochrone.controller.form;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcPrefecture;

import lombok.Data;

@Data
public class FormParkEdit {

	private Long idRegion;
	private Long idComm2Co; 
	private Long idCommune; 
	private Long idPark;
	private String nameCommune;
	
	// data from DB
	private ParcEtJardin parcEtJardin;
	private ParcPrefecture parcPrefecture;
	private ParkArea parkArea;
	private ParkAreaComputed parkAreaComputed;
	private List<ParkEntrance> parkEntrances;
	
	private String nomParc;
	private Long surface;
	private Boolean computeNeeded=Boolean.FALSE;
	
	// park photo
	private MultipartFile photo;
	
}
