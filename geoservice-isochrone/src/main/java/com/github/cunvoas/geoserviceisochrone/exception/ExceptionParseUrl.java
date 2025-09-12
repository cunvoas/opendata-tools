package com.github.cunvoas.geoserviceisochrone.exception;

/**
 * Exception liée à l'analyse (parsing) d'URL.
 * <p>
 * Utilisée pour signaler une erreur lors de la lecture ou de l'analyse d'une URL.
 * </p>
 * @author cunvoas
 */
public class ExceptionParseUrl extends RuntimeException {
	
	private static final long serialVersionUID = -6672246774863253226L;

	/**
	 * Construit une nouvelle exception d'analyse d'URL avec un message explicite.
	 * @param message le message d'erreur
	 */
	public ExceptionParseUrl(String message) {
		super(message);
	}
}