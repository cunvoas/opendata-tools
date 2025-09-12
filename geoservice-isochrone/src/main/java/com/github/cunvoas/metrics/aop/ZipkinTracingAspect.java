package com.github.cunvoas.metrics.aop;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Aspect AOP pour la surveillance Zipkin.
 * Permet de tracer l'exécution des méthodes du package geoserviceisochrone
 * via l'observation Micrometer et Zipkin si l'option est activée.
 */
@Aspect
@Component
@ConditionalOnProperty(
        name="management.tracing.enabled",
        havingValue="true")
public class ZipkinTracingAspect {

    /**
     * Registre d'observation utilisé pour le traçage.
     */
    private final ObservationRegistry observationRegistry;

    /**
     * Constructeur.
     * @param observationRegistry registre d'observation Micrometer
     */
    public ZipkinTracingAspect(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    /**
     * Intercepte et trace l'exécution des méthodes du package cible.
     * @param joinPoint point d'exécution intercepté
     * @return résultat de la méthode interceptée
     * @throws Throwable en cas d'erreur lors de l'exécution de la méthode
     */
    @Around("execution(* com.github.cunvoas.geoserviceisochrone.*.*(..))")
    public Object traceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        return Observation.createNotStarted(methodName, observationRegistry)
            .observe(() -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            });
    }
}