package com.github.cunvoas.geoserviceisochrone.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.github.cunvoas.geoserviceisochrone.config.security.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * Configuration de sécurité pour les API REST avec JWT.
 * Cette configuration s'applique spécifiquement aux endpoints /api/** pour les applications mobiles.
 */
@Configuration
@RequiredArgsConstructor
public class JwtSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Chaîne de filtres de sécurité pour les API REST avec JWT.
     * Cette configuration est prioritaire (Order 0) pour intercepter les requêtes /api/** avant le filtre MVC.
     * 
     * @param http configuration HTTP
     * @return la chaîne de filtres de sécurité
     * @throws Exception en cas d'erreur de configuration
     */
    @Bean
    @Order(0)
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        return http
                // S'applique uniquement aux endpoints /api/**
                .securityMatcher("/api/**")
                
                // Configuration des autorisations
                .authorizeHttpRequests(auth -> auth
                        // Endpoints publics (authentification)
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/refresh",
                                "/api/auth/register"  // Si vous ajoutez l'inscription
                        ).permitAll()
                        // Tous les autres endpoints /api/** nécessitent une authentification
                        .anyRequest().authenticated()
                )
                
                // Désactiver CSRF pour les API REST (utilisation de JWT)
//                .csrf(csrf -> csrf.disable())
                
                // Session stateless (pas de session HTTP, uniquement JWT)
                .sessionManagement(session -> 
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // Ajouter le filtre JWT avant le filtre d'authentification standard
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                
                .build();
    }
}
