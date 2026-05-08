package com.github.cunvoas.geoserviceisochrone.exception;

/**
 * Exception levée lors d'erreurs liées aux refresh tokens JWT.
 */
public class JwtRefreshTokenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public JwtRefreshTokenException(String message) {
        super(message);
    }

    public JwtRefreshTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
