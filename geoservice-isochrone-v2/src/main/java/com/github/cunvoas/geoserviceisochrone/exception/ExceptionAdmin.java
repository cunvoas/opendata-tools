package com.github.cunvoas.geoserviceisochrone.exception;

/**
 * Exception spécifique à la gestion des administrateurs et des règles d'inscription.
 * <p>
 * Utilisée pour signaler des erreurs lors de la gestion des comptes administrateurs, telles que l'existence d'un email ou d'un login, ou un mot de passe non conforme.
 * </p>
 * @author cunvoas
 */
public class ExceptionAdmin extends RuntimeException {
	
	/** Code d'erreur : l'email existe déjà. */
	public static final String RG_EMAIL_EXISTS="RG_EMAIL_EXISTS";
	/** Code d'erreur : le login existe déjà. */
	public static final String RG_LOGIN_EXISTS="RG_LOGIN_EXISTS";
	/** Code d'erreur : le mot de passe n'est pas conforme. */
	public static final String RG_PWD_NOT_SAFE="RG_PWD_NOT_SAFE";
	/** Code d'erreur : cas impossible rencontré. */
	public static final String RG_IMPOSSIBLE_CASE="RG_IMPOSSIBLE_CASE";
	
	private static final long serialVersionUID = -6672246774863253226L;

	/**
	 * Construit une nouvelle exception administrateur avec un message explicite.
	 * @param message le message d'erreur
	 */
	public ExceptionAdmin(String message) {
		super(message);
	}
}