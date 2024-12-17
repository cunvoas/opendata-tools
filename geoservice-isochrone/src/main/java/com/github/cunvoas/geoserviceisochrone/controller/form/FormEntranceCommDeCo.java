package com.github.cunvoas.geoserviceisochrone.controller.form;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class FormEntranceCommDeCo {

	private VoItems communautesDeCommune;
	private List<VoItems> communautesDeCommunes;
	
}
