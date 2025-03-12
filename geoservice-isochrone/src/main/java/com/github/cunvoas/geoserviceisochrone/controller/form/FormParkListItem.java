package com.github.cunvoas.geoserviceisochrone.controller.form;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for park list page.
 */
@Data
@EqualsAndHashCode(callSuper=false)
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
