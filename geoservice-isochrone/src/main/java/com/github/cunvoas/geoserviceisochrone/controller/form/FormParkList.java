package com.github.cunvoas.geoserviceisochrone.controller.form;

import java.util.List;

import com.github.cunvoas.geoserviceisochrone.model.isochrone.ParkType;
import com.github.cunvoas.geoserviceisochrone.model.opendata.City;
import com.github.cunvoas.geoserviceisochrone.model.opendata.CommunauteCommune;
import com.github.cunvoas.geoserviceisochrone.model.opendata.Region;

import lombok.Data;

@Data
public class FormParkList extends AbstractFormLocate{
	private Long idParkType;
	private String parkCase="all";

	private  List<ParkType> listTypePark;
	
	
	private Integer page=1;
	private Integer size=300;
}
