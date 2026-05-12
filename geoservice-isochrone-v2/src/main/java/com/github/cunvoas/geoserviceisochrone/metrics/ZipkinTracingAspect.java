package com.github.cunvoas.geoserviceisochrone.metrics;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

/**
 * Aspect AOP pour le traçage (tracing) des méthodes.
 *
 * <p>Lorsque la propriété {@code management.tracing.enabled} est activée,
 * cet aspect enveloppe l'exécution des méthodes du package principal et publie
 * des observations dans {@link ObservationRegistry}. Ces observations peuvent être
 * exportées vers Zipkin ou un autre système de traçage supporté par Micrometer.</p>
 *
 * <p>Le point d'interception cible les méthodes du package racine
 * {@code com.github.cunvoas.geoserviceisochrone}.</p>
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
    @Autowired
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
