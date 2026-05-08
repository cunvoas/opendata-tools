package com.github.cunvoas.geoserviceisochrone.exception;

/**
 * Exception spécifique aux traitements géographiques.
 * <p>
 * Utilisée pour signaler des erreurs lors des opérations géospatiales, comme les erreurs de fusion.
 * </p>
 * @author cunvoas
 */
public class ExceptionGeo extends RuntimeException {
	
	/** Code d'erreur pour une erreur de fusion géographique. */
	public static final String MERGE="MERGE_ERROR";
	
	private static final long serialVersionUID = -6672246774863253226L;

	/**
	 * Construit une nouvelle exception géographique avec un message explicite.
	 * @param message le message d'erreur
	 */
	public ExceptionGeo(String message) {
		super(message);
	}
}