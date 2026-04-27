package com.github.cunvoas.geoserviceisochrone.controller.form;

import java.util.List;

import com.github.cunvoas.geoserviceisochrone.model.admin.ComputeJobStat;

import lombok.Data;

/**
 * Form for ComputeJob page.
 */
@Data
public class FormComputeJob {
	
	private List<ComputeJobStat> stats;
	
	public boolean hasStats(){
		return stats!=null && !stats.isEmpty();
	}
	
	private String requestCity; // insee code
	
	private Integer requestPark; // parkId
	
	
}
