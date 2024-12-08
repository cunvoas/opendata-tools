package com.github.cunvoas.geoserviceisochrone.controller.form;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkArea;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkAreaComputed;
import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcEtJardin;
import com.github.cunvoas.geoserviceisochrone.model.opendata.ParcPrefecture;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class FormParkEdit extends AbstractFormLocate {

	private Long idPark;//parcEtJardin

	// data from DB
	private ParcEtJardin parcEtJardin;
	private ParcPrefecture parcPrefecture;
	private ParkArea parkArea;
	private List<ParkEntrance> parkEntrances;
	private ParkAreaComputed parkAreaComputed;
	
	private String nomParc;
	private Long surface;
	private Long surfaceContour;
	private Boolean computeNeeded=Boolean.FALSE;
	
	
	// park photo
	private MultipartFile photo;
	
	//edit fields
	//parkArea
	private String description;
	private Long idParkType;	//ParkType
	
	//parkAreaCompute
	private Boolean oms;
	//parkPrefecture
	private Long idParkAndGarden;//parcEtJardin
	
}
