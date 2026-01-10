package com.github.cunvoas.geoserviceisochrone.service.jwt;

import com.github.cunvoas.geoserviceisochrone.config.security.jwt.JwtUtil;
import com.github.cunvoas.geoserviceisochrone.exception.JwtRefreshTokenException;
import com.github.cunvoas.geoserviceisochrone.model.admin.Contributeur;
import com.github.cunvoas.geoserviceisochrone.model.jwt.RefreshToken;
import com.github.cunvoas.geoserviceisochrone.repo.jwt.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service d'authentification JWT.
 * Gère la génération et la validation des tokens JWT et refresh tokens.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenDurationMs;

    /**
     * Authentifie un utilisateur et génère les tokens.
     * @param username le nom d'utilisateur
     * @param password le mot de passe
     * @return le contributeur authentifié
     * @throws AuthenticationException si l'authentification échoue
     */
    @Transactional
    public Contributeur authenticate(String username, String password) throws AuthenticationException {
        log.info("[JWT Auth Service] Tentative d'authentification pour l'utilisateur: {}", username);
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            Contributeur contributeur = (Contributeur) authentication.getPrincipal();
            log.info("[JWT Auth Service] Utilisateur {} authentifié avec succès (ID: {})", username, contributeur.getId());
            log.debug("[JWT Auth Service] Rôles: {}", authentication.getAuthorities());
            
            return contributeur;
        } catch (AuthenticationException e) {
            log.error("[JWT Auth Service] Échec d'authentification pour l'utilisateur {}: {}", username, e.getMessage());
            throw e;
        }
    }

    /**
     * Génère un access token JWT.
     * @param contributeur le contributeur
     * @return le token JWT
     */
    public String generateAccessToken(Contributeur contributeur) {
        log.info("[JWT Auth Service] Génération d'un access token pour l'utilisateur: {}", contributeur.getLogin());
        String token = jwtUtil.generateToken(contributeur);
        log.debug("[JWT Auth Service] Access token généré avec succès");
        return token;
    }

    /**
     * Crée ou renouvelle un refresh token pour un contributeur.
     * @param contributeur le contributeur
     * @return le refresh token
     */
    @Transactional
    public RefreshToken createRefreshToken(Contributeur contributeur) {
        log.info("[JWT Auth Service] Création d'un refresh token pour l'utilisateur: {}", contributeur.getLogin());
        // Supprimer les anciens tokens du contributeur
        refreshTokenRepository.deleteByContributeur(contributeur);
        log.debug("[JWT Auth Service] Anciens tokens supprimés pour: {}", contributeur.getLogin());

        // Créer un nouveau refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .contributeur(contributeur)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000))
                .revoked(false)
                .build();

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.info("[JWT Auth Service] Refresh token créé avec succès pour: {} (Expire le: {})", 
                contributeur.getLogin(), savedToken.getExpiryDate());
        return savedToken;
    }

    /**
     * Vérifie et récupère un refresh token.
     * @param token le token à vérifier
     * @return le refresh token s'il est valide
     * @throws JwtRefreshTokenException si le token est invalide ou expiré
     */
    public RefreshToken verifyRefreshToken(String token) {
        log.debug("[JWT Auth Service] Vérification du refresh token");
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);
        
        if (refreshTokenOpt.isEmpty()) {
            log.error("[JWT Auth Service] Refresh token introuvable");
            throw new JwtRefreshTokenException("Refresh token introuvable");
        }

        RefreshToken refreshToken = refreshTokenOpt.get();
        String username = refreshToken.getContributeur().getLogin();

        if (refreshToken.isRevoked()) {
            log.error("[JWT Auth Service] Refresh token révoqué pour l'utilisateur: {}", username);
            throw new JwtRefreshTokenException("Refresh token révoqué");
        }

        if (refreshToken.isExpired()) {
            log.warn("[JWT Auth Service] Refresh token expiré pour l'utilisateur: {} (Expiré le: {})", 
                    username, refreshToken.getExpiryDate());
            refreshTokenRepository.delete(refreshToken);
            throw new JwtRefreshTokenException("Refresh token expiré");
        }

        log.debug("[JWT Auth Service] Refresh token valide pour l'utilisateur: {}", username);
        return refreshToken;
    }

    /**
     * Révoque un refresh token.
     * @param token le token à révoquer
     */
    @Transactional
    public void revokeRefreshToken(String token) {
        log.info("[JWT Auth Service] Révocation d'un refresh token");
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);
        refreshTokenOpt.ifPresent(refreshToken -> {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            log.info("[JWT Auth Service] Refresh token révoqué pour l'utilisateur: {}", refreshToken.getContributeur().getLogin());
        });
        if (refreshTokenOpt.isEmpty()) {
            log.warn("[JWT Auth Service] Token à révoquer introuvable");
        }
    }

    /**
     * Révoque tous les tokens d'un contributeur.
     * @param contributeur le contributeur
     */
    @Transactional
    public void revokeAllUserTokens(Contributeur contributeur) {
        log.warn("[JWT Auth Service] Révocation de tous les tokens pour l'utilisateur: {}", contributeur.getLogin());
        refreshTokenRepository.deleteByContributeur(contributeur);
        log.info("[JWT Auth Service] Tous les tokens de l'utilisateur {} ont été révoqués", contributeur.getLogin());
    }
}
