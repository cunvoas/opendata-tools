package com.github.cunvoas.geoserviceisochrone.monitor;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Filtre dédié au logging des requêtes Prometheus.
 * <p>
 * Les logs sont routés vers le fichier {@code activity-monitoring.log}
 * via le logger {@code com.github.cunvoas.geoserviceisochrone.monitor.PrometheusFilter}
 * configuré dans {@code logback-spring.xml} (appender MONITOR_FILE).
 * </p>
 */
@Slf4j
@Component
public class PrometheusFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if (log.isInfoEnabled()) {
            log.info("{} {} from {}", request.getMethod(), path, request.getRemoteAddr());
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // ne filtrer que les requêtes Prometheus
        return !path.contains("/actuator/prometheus");
    }
}
