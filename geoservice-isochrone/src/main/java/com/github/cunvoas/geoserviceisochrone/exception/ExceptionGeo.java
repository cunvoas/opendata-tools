package com.github.cunvoas.geoserviceisochrone.exception;

/**
 * @author cus
 */
public class ExceptionGeo extends RuntimeException {
	
	public static final String MERGE="MERGE_ERROR";
	
	private static final long serialVersionUID = -6672246774863253226L;

	public ExceptionGeo(String message) {
		super(message);
	}
}
