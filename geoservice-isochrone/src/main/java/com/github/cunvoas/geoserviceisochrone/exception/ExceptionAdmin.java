package com.github.cunvoas.geoserviceisochrone.exception;

/**
 * @author cus
 */
public class ExceptionAdmin extends RuntimeException {
	
	public static final String RG_EMAIL_EXISTS="RG_EMAIL_EXISTS";
	public static final String RG_LOGIN_EXISTS="RG_LOGIN_EXISTS";
	public static final String RG_PWD_NOT_SAFE="RG_PWD_NOT_SAFE";
	
	
	private static final long serialVersionUID = -6672246774863253226L;

	public ExceptionAdmin(String message) {
		super(message);
		
		//todo exception management.
	}
}
