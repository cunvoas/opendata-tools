package com.github.cunvoas.geoserviceisochrone.service.park;

import java.math.BigDecimal;

import lombok.Data;

/**
 * DTO to factorise compute method.
 */
@Data
public class ComputeDto {
	
	private BigDecimal surfacePerCapitaForIsochroneOnSquare = BigDecimal.ZERO;
	private BigDecimal populationInIsochrone = BigDecimal.ZERO;
	
}
