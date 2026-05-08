package com.github.cunvoas.geoserviceisochrone.exception;

/**
 * Exception liée aux erreurs lors de l'extraction de données.
 * <p>
 * Utilisée pour signaler un problème lors de l'extraction d'informations ou de fichiers.
 * </p>
 * @author cunvoas
 */
public class ExceptionExtract extends RuntimeException {
	
	private static final long serialVersionUID = -6672246774863253226L;

	/**
	 * Construit une nouvelle exception d'extraction avec un message explicite.
	 * @param message le message d'erreur
	 */
	public ExceptionExtract(String message) {
		super(message);
	}
}