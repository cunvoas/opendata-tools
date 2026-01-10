package com.github.cunvoas.geoserviceisochrone.controller.mobile.auth;

import com.github.cunvoas.geoserviceisochrone.controller.mobile.auth.dto.AuthResponse;
import com.github.cunvoas.geoserviceisochrone.controller.mobile.auth.dto.LoginRequest;
import com.github.cunvoas.geoserviceisochrone.controller.mobile.auth.dto.RefreshTokenRequest;
import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.jwt.RefreshToken;
import com.github.cunvoas.geoserviceisochrone.service.jwt.JwtAuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST pour l'authentification JWT.
 * Fournit les endpoints pour le login, le refresh token et le logout.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class JwtController {

    private final JwtAuthService jwtAuthService;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /**
     * Endpoint de login.
     * @param loginRequest les identifiants de connexion
     * @return la réponse d'authentification avec les tokens
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("[JWT Controller] Tentative de login pour l'utilisateur: {}", loginRequest.getUsername());
        try {
            // Authentifier l'utilisateur
            Contributeur contributeur = jwtAuthService.authenticate(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            );

            // Générer les tokens
            log.debug("[JWT Controller] Génération des tokens pour: {}", contributeur.getLogin());
            String accessToken = jwtAuthService.generateAccessToken(contributeur);
            RefreshToken refreshToken = jwtAuthService.createRefreshToken(contributeur);

            // Construire la réponse
            AuthResponse response = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(jwtExpiration / 1000) // en secondes
                    .userId(contributeur.getId())
                    .username(contributeur.getLogin())
                    .email(contributeur.getEmail())
                    .fullName(contributeur.getFullName())
                    .role(contributeur.getRole().name())
                    .build();

            log.info("[JWT Controller] Login réussi pour l'utilisateur: {} (ID: {})", contributeur.getLogin(), contributeur.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[JWT Controller] Erreur lors de l'authentification pour l'utilisateur {}: {}", 
                    loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Identifiants invalides");
        }
    }

    /**
     * Endpoint de rafraîchissement de token.
     * @param request la requête contenant le refresh token
     * @return une nouvelle paire de tokens
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("[JWT Controller] Tentative de rafraîchissement du token");
        try {
            // Vérifier le refresh token
            RefreshToken refreshToken = jwtAuthService.verifyRefreshToken(request.getRefreshToken());
            Contributeur contributeur = refreshToken.getContributeur();
            log.debug("[JWT Controller] Refresh token valide pour l'utilisateur: {}", contributeur.getLogin());

            // Générer un nouveau access token
            String newAccessToken = jwtAuthService.generateAccessToken(contributeur);

            // Optionnel: générer aussi un nouveau refresh token
            RefreshToken newRefreshToken = jwtAuthService.createRefreshToken(contributeur);

            // Construire la réponse
            AuthResponse response = AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(jwtExpiration / 1000)
                    .userId(contributeur.getId())
                    .username(contributeur.getLogin())
                    .email(contributeur.getEmail())
                    .fullName(contributeur.getFullName())
                    .role(contributeur.getRole().name())
                    .build();

            log.info("[JWT Controller] Token rafraîchi avec succès pour l'utilisateur: {}", contributeur.getLogin());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[JWT Controller] Erreur lors du rafraîchissement du token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Refresh token invalide ou expiré");
        }
    }

    /**
     * Endpoint de logout.
     * @param authentication l'authentification de l'utilisateur
     * @return la confirmation du logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal Authentication authentication) {
        log.info("[JWT Controller] Tentative de logout");
        try {
            if (authentication != null && authentication.getPrincipal() instanceof Contributeur) {
                Contributeur contributeur = (Contributeur) authentication.getPrincipal();
                log.debug("[JWT Controller] Révocation de tous les tokens pour l'utilisateur: {}", contributeur.getLogin());
                jwtAuthService.revokeAllUserTokens(contributeur);
                log.info("[JWT Controller] Logout réussi pour l'utilisateur: {}", contributeur.getLogin());
                return ResponseEntity.ok("Déconnexion réussie");
            }
            log.warn("[JWT Controller] Tentative de logout sans authentification");
            return ResponseEntity.ok("Déconnexion réussie");
        } catch (Exception e) {
            log.error("[JWT Controller] Erreur lors de la déconnexion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la déconnexion");
        }
    }

    /**
     * Endpoint pour vérifier l'état de l'authentification.
     * @param contributeur le contributeur authentifié
     * @return les informations de l'utilisateur
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal Contributeur contributeur) {
        if (contributeur == null) {
            log.warn("[JWT Controller] Tentative d'accès à /me sans authentification");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Non authentifié");
        }

        log.debug("[JWT Controller] Récupération des informations de l'utilisateur: {}", contributeur.getLogin());
        AuthResponse response = AuthResponse.builder()
                .userId(contributeur.getId())
                .username(contributeur.getLogin())
                .email(contributeur.getEmail())
                .fullName(contributeur.getFullName())
                .role(contributeur.getRole().name())
                .build();

        return ResponseEntity.ok(response);
    }
}
