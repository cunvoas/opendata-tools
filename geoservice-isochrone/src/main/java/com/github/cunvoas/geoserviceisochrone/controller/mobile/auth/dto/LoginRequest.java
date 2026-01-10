package com.github.cunvoas.geoserviceisochrone.controller.mobile.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO pour la requête de login.
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Le nom d'utilisateur est requis")
    private String username;

    @NotBlank(message = "Le mot de passe est requis")
    private String password;
}
