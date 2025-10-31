package com.github.cunvoas.geoserviceisochrone.model.analytics;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = {"annee", "surfaceMin", "surfaceMax"})
public class StatsSurfaceId {

	private Integer annee;
	private Integer surfaceMin;
	private Integer surfaceMax;
	
}
