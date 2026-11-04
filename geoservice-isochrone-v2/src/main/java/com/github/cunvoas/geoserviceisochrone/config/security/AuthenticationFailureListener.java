package com.github.cunvoas.geoserviceisochrone.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import com.github.cunvoas.geoserviceisochrone.util.LogSanitizer;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Intercepteur d'échec d'authentification.
 * <p>
 * Permet de compter les tentatives échouées pour un blocage éventuel par IP.
 * </p>
 */
@Component
@Slf4j
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private LoginAttemptService loginAttemptService;

    /**
     * Incrémente le compteur d'échecs pour l'adresse IP du client lors d'un échec d'authentification.
     * @param e événement d'échec d'authentification
     */
    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent e) {
        String ip = request.getRemoteAddr();
        loginAttemptService.loginFailed(ip);
        log.error("IP reported {}", LogSanitizer.sanitize(ip));
    }
}