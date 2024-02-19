package com.github.cunvoas.geoserviceisochrone.controller.form;

import java.util.Date;

import lombok.Data;

@Data
public class FormParkListItem {

	private Long idRegion;
	private Long idCommunauteDeCommunes;
	private Long idCommune;
	private Long idPark;
	private Long idArea;

	private String nameRegion;
	private String nameCommunauteDeCommunes;
	private String nameCommune;
	private String nameQuartier;
	private String namePark;

	private Boolean oms = Boolean.TRUE;
	private String type;
	private Date lastEntranceUpdate;
	private Date lastIsochroneUpdate;
	
}
