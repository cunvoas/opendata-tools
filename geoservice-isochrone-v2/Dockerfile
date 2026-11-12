# Spring Boot 4 / Java 25 Dockerfile
FROM docker.io/bellsoft/hardened-liberica-runtime-container:jdk-27-musl@sha256:42abfe1a78915432299b2cd96e6f724952696dceb6b9d4e1a65aeca243fec9a6

COPY --chown=1001:1001 target/*.jar /app/app.jar
USER 1001:1001
RUN chmod 554 /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
