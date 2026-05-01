package com.github.cunvoas.geoserviceisochrone.monitor;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Filter pour logger tous les accès aux URLs contenant "prometheus" dans monitor.log (prod).
 * Le logger doit être configuré dans logback-spring.xml pour être routé dans le fichier dédié.
 */
@Component
@Slf4j
public class PrometheusFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest req) {
            String uri = req.getRequestURI();
            if (uri != null && uri.toLowerCase().contains("prometheus")) {
                log.info("Access to prometheus endpoint: {} {}", req.getMethod(), uri);
            }
        }
        chain.doFilter(request, response);
    }
}
