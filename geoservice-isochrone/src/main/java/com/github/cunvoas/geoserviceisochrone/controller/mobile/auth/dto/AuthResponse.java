package com.github.cunvoas.geoserviceisochrone.controller.mobile.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la réponse d'authentification.
 * Contient les tokens JWT et les informations de l'utilisateur.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    
    // Informations de l'utilisateur
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String role;
}
