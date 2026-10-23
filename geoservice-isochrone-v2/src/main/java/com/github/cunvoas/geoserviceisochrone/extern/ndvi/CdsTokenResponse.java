package com.github.cunvoas.geoserviceisochrone.extern.ndvi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Réponse du service d'authentification OIDC (Keycloak) de Copernicus Data Space.
 * @see https://documentation.dataspace.copernicus.eu/APIs/Token.html
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CdsTokenResponse(
    @JsonProperty("access_token")
    String accessToken,

    @JsonProperty("expires_in")
    Long expiresIn,

    @JsonProperty("refresh_token")
    String refreshToken
) {}
