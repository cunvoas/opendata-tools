# Spring Boot 4 / Java 25 Dockerfile
FROM docker.io/bellsoft/hardened-liberica-runtime-container:jdk-24-musl@sha256:da1a3caf874c172c6c08afa8ebabc7ac94e93e914c1fb6b6460d06b72b74ee9a

COPY --chown=1001:1001 target/*.jar /app/app.jar
USER 1001:1001
RUN chmod 554 /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
