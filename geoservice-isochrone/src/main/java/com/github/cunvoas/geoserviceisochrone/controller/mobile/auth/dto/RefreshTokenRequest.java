package com.github.cunvoas.geoserviceisochrone.controller.mobile.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO pour la requête de rafraîchissement de token.
 */
@Data
public class RefreshTokenRequest {

    @NotBlank(message = "Le refresh token est requis")
    private String refreshToken;
}
