package com.github.cunvoas.geoserviceisochrone.config.security.jwt;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * Utilitaire pour la gestion des tokens JWT.
 * Permet de générer, valider et extraire des informations des tokens JWT.
 */
@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    /**
     * Extrait le nom d'utilisateur du token JWT.
     * @param token le token JWT
     * @return le nom d'utilisateur
     */
    public String extractUsername(String token) {
        try {
            String username = extractClaim(token, Claims::getSubject);
            log.debug("[JWT Util] Nom d'utilisateur extrait: {}", username);
            return username;
        } catch (Exception e) {
            log.warn("[JWT Util] Erreur lors de l'extraction du nom d'utilisateur: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Extrait une claim spécifique du token.
     * @param token le token JWT
     * @param claimsResolver la fonction pour extraire la claim
     * @param <T> le type de la claim
     * @return la valeur de la claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Génère un token JWT pour l'utilisateur.
     * @param userDetails les détails de l'utilisateur
     * @return le token JWT
     */
    public String generateToken(UserDetails userDetails) {
        log.info("[JWT Util] Génération d'un token JWT pour l'utilisateur: {}", userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        String token = generateToken(claims, userDetails.getUsername());
        log.debug("[JWT Util] Token JWT généré avec succès pour: {}", userDetails.getUsername());
        return token;
    }

    /**
     * Génère un token JWT avec des claims personnalisées.
     * @param extraClaims les claims supplémentaires
     * @param username le nom d'utilisateur
     * @return le token JWT
     */
    public String generateToken(Map<String, Object> extraClaims, String username) {
        return buildToken(extraClaims, username, jwtExpiration);
    }

    /**
     * Génère un refresh token.
     * @param username le nom d'utilisateur
     * @return le refresh token
     */
    public String generateRefreshToken(String username) {
        log.info("[JWT Util] Génération d'un refresh token pour l'utilisateur: {}", username);
        String token = buildToken(new HashMap<>(), username, refreshExpiration);
        log.debug("[JWT Util] Refresh token généré avec succès pour: {}", username);
        return token;
    }

    /**
     * Construit un token JWT.
     * @param extraClaims les claims supplémentaires
     * @param username le nom d'utilisateur
     * @param expiration la durée d'expiration en millisecondes
     * @return le token JWT
     */
    private String buildToken(
            Map<String, Object> extraClaims,
            String username,
            long expiration
    ) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Valide un token JWT.
     * @param token le token JWT
     * @param userDetails les détails de l'utilisateur
     * @return true si le token est valide, false sinon
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean isExpired = isTokenExpired(token);
            boolean usernameMatches = username.equals(userDetails.getUsername());
            
            if (log.isDebugEnabled()) {
                log.debug("[JWT Util] Validation du token - Utilisateur: {}, Match: {}, Expiré: {}", 
                        username, usernameMatches, isExpired);
            }
            
            boolean isValid = usernameMatches && !isExpired;
            
            if (!isValid) {
                log.warn("[JWT Util] Token invalide pour l'utilisateur: {} (Match: {}, Expiré: {})", 
                        username, usernameMatches, isExpired);
            } else {
                log.debug("[JWT Util] Token valide pour l'utilisateur: {}", username);
            }
            
            return isValid;
        } catch (Exception e) {
            log.error("[JWT Util] Erreur lors de la validation du token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Vérifie si un token est expiré.
     * @param token le token JWT
     * @return true si le token est expiré, false sinon
     */
    private boolean isTokenExpired(String token) {
        Date expirationDate = extractExpiration(token);
        boolean isExpired = expirationDate.before(new Date());
        if (log.isDebugEnabled()) {
            log.debug("[JWT Util] Vérification d'expiration - Expiré le: {}, Expiré: {}", expirationDate, isExpired);
        }
        return isExpired;
    }

    /**
     * Extrait la date d'expiration du token.
     * @param token le token JWT
     * @return la date d'expiration
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrait toutes les claims du token.
     * @param token le token JWT
     * @return les claims
     */
    private Claims extractAllClaims(String token) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("[JWT Util] Extraction des claims du token");
            }
            Claims claims = Jwts.parser()
                    .verifyWith(getSignInKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            log.debug("[JWT Util] Claims extraites avec succès");
            return claims;
        } catch (Exception e) {
            log.error("[JWT Util] Erreur lors de l'extraction des claims: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Obtient la clé de signature.
     * @return la clé de signature
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
