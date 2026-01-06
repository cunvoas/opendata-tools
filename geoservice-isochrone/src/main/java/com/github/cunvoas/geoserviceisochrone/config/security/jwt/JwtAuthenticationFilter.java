package com.github.cunvoas.geoserviceisochrone.config.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre d'authentification JWT.
 * Intercepte chaque requête pour valider le token JWT et authentifier l'utilisateur.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Vérifie si le header Authorization contient un Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            if (log.isDebugEnabled()) {
                log.debug("[JWT Filter] Pas de header Authorization ou pas de Bearer token - URL: {}", request.getRequestURI());
            }
            filterChain.doFilter(request, response);
            return;
        }

        // Extrait le token du header
        jwt = authHeader.substring(7);
        log.debug("[JWT Filter] Token JWT reçu pour l'URL: {}", request.getRequestURI());
        
        try {
            // Extrait le nom d'utilisateur du token
            username = jwtUtil.extractUsername(jwt);
            log.debug("[JWT Filter] Nom d'utilisateur extrait du token: {}", username);

            // Si l'utilisateur n'est pas encore authentifié
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                log.debug("[JWT Filter] Tentative de chargement des détails de l'utilisateur: {}", username);
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Valide le token
                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    log.info("[JWT Filter] Token JWT valide pour l'utilisateur: {} - URL: {}", username, request.getRequestURI());
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    // Définit l'authentification dans le contexte de sécurité
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("[JWT Filter] Authentification établie pour l'utilisateur: {} avec rôles: {}", 
                            username, userDetails.getAuthorities());
                } else {
                    log.warn("[JWT Filter] Token JWT invalide pour l'utilisateur: {} - URL: {}", username, request.getRequestURI());
                }
            } else if (username == null) {
                log.warn("[JWT Filter] Impossible d'extraire le nom d'utilisateur du token");
            } else {
                log.debug("[JWT Filter] Utilisateur {} déjà authentifié", username);
            }
        } catch (Exception e) {
            log.error("[JWT Filter] Erreur lors de l'authentification JWT pour l'URL {}: {}", 
                    request.getRequestURI(), e.getMessage(), e);
        }

        filterChain.doFilter(request, response);
    }
}
