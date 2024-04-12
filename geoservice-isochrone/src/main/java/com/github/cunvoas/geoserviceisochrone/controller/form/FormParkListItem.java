package com.github.cunvoas.geoserviceisochrone.controller.form;

import java.util.Date;

import lombok.Data;

@Data
public class FormParkListItem extends AbstractFormLocate{

	private Long idPark;
	private Long idArea;

	private String nameQuartier;
	private String namePark;

	private Boolean oms = Boolean.TRUE;
	private String type;
	private Date lastEntranceUpdate;
	private Date lastIsochroneUpdate;
	
}
