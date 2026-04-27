package com.github.cunvoas.geoserviceisochrone.exception;

/**
 * Exception liée au traitement des photos.
 * <p>
 * Utilisée pour signaler une erreur lors de la gestion ou du traitement d'une photo.
 * </p>
 * @author cunvoas
 */
public class ExceptionPhoto extends RuntimeException {
	
	private static final long serialVersionUID = -6672246774863253226L;

	/**
	 * Construit une nouvelle exception photo avec un message explicite.
	 * @param message le message d'erreur
	 */
	public ExceptionPhoto(String message) {
		super(message);
	}
}