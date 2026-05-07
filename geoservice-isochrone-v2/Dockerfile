# Spring Boot 4 / Java 25 Dockerfile
FROM docker.io/bellsoft/hardened-liberica-runtime-container:jdk-21-musl

COPY --chown=1001:1001 target/*.jar /app/app.jar
USER 1001:1001
RUN chmod 554 /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
