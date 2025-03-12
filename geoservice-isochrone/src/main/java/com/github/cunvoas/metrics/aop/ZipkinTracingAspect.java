package com.github.cunvoas.metrics.aop;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * AOP for Zipkin monitoring.
 */
@Aspect
@Component
@ConditionalOnProperty(
		name="management.tracing.enabled", 
		havingValue="true")
public class ZipkinTracingAspect {

    private final ObservationRegistry observationRegistry;

    public ZipkinTracingAspect(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    /**
     * Embrace method call.
     * @param joinPoint
     * @return
     * @throws Throwable
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