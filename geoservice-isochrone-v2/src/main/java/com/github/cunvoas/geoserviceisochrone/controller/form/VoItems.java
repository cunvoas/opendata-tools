package com.github.cunvoas.geoserviceisochrone.controller.form;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO for items in geojson.
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class VoItems {
	private Integer id;
	private String libelle;
}
