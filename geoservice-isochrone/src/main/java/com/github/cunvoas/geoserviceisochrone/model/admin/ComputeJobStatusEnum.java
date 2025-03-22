package com.github.cunvoas.geoserviceisochrone.model.admin;
	
/**
 * Source du parc.
 */
public enum ComputeJobStatusEnum {
	// int code in database
	TO_PROCESS("TO_PROCESS"),  // 0 process requested
	IN_PROCESS("IN_PROCESS"),  // 1 process started
	PROCESSED("PROCESSED"),	   // 2 process done
	IN_ERROR("IN_ERROR"),	   // 3 process in error
	;
	
	private String status;

	/**
	 * Constructor.
	 * @param status value
	 */
	ComputeJobStatusEnum(String status) {
		this.status = status;
	}

	/**
	 * @return status value
	 */
	public String getStatus() {
		return status;
	}
	
	/**
	 * @see java.lang.Object.toString()
	 */
	@Override
	public String toString() {
		return this.getStatus();
	}
}
