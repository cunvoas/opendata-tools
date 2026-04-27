# Spring Boot 4 / Java 25 Dockerfile
FROM docker.io/bellsoft/hardened-liberica-runtime-container:jdk-21-musl

COPY target/*.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
