package com.github.cunvoas.geoserviceisochrone.controller.form;

import java.util.Date;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkEntrance;

import lombok.Data;

@Data
public class FormParkEntranceDetail {
	
	public FormParkEntranceDetail() {
		super();
	}
	
	public FormParkEntranceDetail(FormParkEntrance form) {
		idRegion = form.getIdRegion();
		idCommunauteDeCommunes = form.getIdCommunauteDeCommunes();
		idCommune = form.getIdCommune();
		idPark = form.getIdPark();
	}
	
	public FormParkEntranceDetail(ParkEntrance bo, FormParkEntrance form) {
		super();
		idRegion = form.getIdRegion();
		idCommunauteDeCommunes = form.getIdCommunauteDeCommunes();
		idCommune = form.getIdCommune();
		idPark = form.getIdPark();
		areaId= form.getAreaId();
		idEntrance = form.getIdEntrance();
		
		if (bo!=null) {
			entranceId = bo.getId();
			if (bo.getParkArea()!=null) {
				areaId= bo.getParkArea().getId();
			}
			description = bo.getDescription();
			ignDate = bo.getIgnDate();
			updateDate  = bo.getUpdateDate();
			entranceLink = bo.getEntranceLink();
			entranceLat = Double.valueOf(bo.getEntryLat());
			entranceLng = Double.valueOf(bo.getEntryLng());
			entrancePolygon = String.valueOf(bo.getPolygon());
		}
	}
	
	private String mode = "upd";
	
	// parent form ids
	private Long idRegion;
	private Long idCommunauteDeCommunes;
	private Long idCommune;
	private Long idPark;
	private Long idEntrance;
	
	private Long entranceId;
	private Long areaId;
	private String description;
	private Date ignDate;
	private Date updateDate;
	private String entranceLink;
	private Double entranceLat;
	private Double entranceLng;
	private String entrancePolygon;
	
	

}
