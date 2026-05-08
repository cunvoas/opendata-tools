package com.github.cunvoas.geoserviceisochrone.controller.form;

import java.util.List;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkType;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Form for Park List page.
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class FormParkList extends AbstractFormLocate{
	private Long idParkType;
	private String parkCase="all";

	private  List<ParkType> listTypePark;
	
	
	private Integer page=1;
	private Integer size=300;
}
