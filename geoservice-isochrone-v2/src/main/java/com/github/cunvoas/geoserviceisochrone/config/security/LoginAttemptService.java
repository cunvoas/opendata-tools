package com.github.cunvoas.geoserviceisochrone.config.security;


import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import jakarta.servlet.http.HttpServletRequest;


/**
 * Service de limitation des tentatives de connexion échouées.
 * <p>
 * Permet de bloquer temporairement un utilisateur après plusieurs échecs d'authentification.
 * </p>
 */
@Service
public class LoginAttemptService {

    /**
     * Nombre maximal de tentatives autorisées avant blocage.
     */
    public static final int MAX_ATTEMPT = 3;
    
    /**
     * Durée du blocage après dépassement du nombre de tentatives (en heures).
     */
    public static final int DELAY_TIME = 2;
    public static final TimeUnit DELAY_UNIT = TimeUnit.HOURS;
    
    private LoadingCache<String, Integer> attemptsCache;

    @Autowired
    private HttpServletRequest request;

    /**
     * Constructeur initialisant le cache des tentatives avec expiration automatique.
     */
    public LoginAttemptService() {
        super();
        attemptsCache = CacheBuilder.newBuilder().expireAfterWrite(DELAY_TIME, DELAY_UNIT).build(new CacheLoader<String, Integer>() {
            @Override
            public Integer load(final String key) {
                return 0;
            }
        });
    }

    /**
     * Incrémente le compteur d'échecs pour une clé (IP).
     * @param key l'adresse IP du client
     */
    public void loginFailed(final String key) {
        int attempts;
        try {
            attempts = attemptsCache.get(key);
        } catch (final ExecutionException e) {
            attempts = 0;
        }
        attempts++;
        attemptsCache.put(key, attempts);
    }

    /**
     * Indique si le client courant est bloqué suite à trop d'échecs.
     * @return true si le client est bloqué, false sinon
     */
    public boolean isBlocked() {
        try {
            return attemptsCache.get(getClientIP()) >= MAX_ATTEMPT;
        } catch (final ExecutionException e) {
            return false;
        }
    }
    
    /**
     * Récupère l'adresse IP du client à partir de la requête HTTP.
     * @return l'adresse IP du client
     */
    private String getClientIP() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || !xfHeader.contains(request.getRemoteAddr())) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}