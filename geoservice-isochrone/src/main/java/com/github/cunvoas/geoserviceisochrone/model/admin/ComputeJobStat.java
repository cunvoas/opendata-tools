package com.github.cunvoas.geoserviceisochrone.model.admin;

import lombok.Data;

/**
 * Aggregate for ComputeJob stats.
 */
@Data
public class ComputeJobStat {
	
	private Integer nb;
	
	private ComputeJobStatusEnum status = ComputeJobStatusEnum.TO_PROCESS;

	private String codeInsee;
}
