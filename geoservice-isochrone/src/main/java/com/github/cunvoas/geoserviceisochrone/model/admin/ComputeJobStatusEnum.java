package com.github.cunvoas.geoserviceisochrone.model.admin;
	
/**
 * Source du parc.
 */
public enum ComputeJobStatusEnum {
	TO_PROCESS("TO_PROCESS"),  // 0 process requested
	IN_PROCESS("IN_PROCESS"),  // 1 process started
	PROCESSED("PROCESSED"),	   // 2 process done
	IN_ERROR("IN_ERROR"),	   // 3 process in error
	;
	
	private String status;

	ComputeJobStatusEnum(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}
	
	@Override
	public String toString() {
		return this.getStatus();
	}
}
