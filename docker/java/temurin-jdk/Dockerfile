FROM eclipse-temurin:21.0.10_7-jdk-alpine-3.22

RUN apk update && apk --no-cache add curl \
    && addgroup -S app \
    && adduser -S -G app app

WORKDIR /app

COPY --chown=app:app geoservice-isochrone-*.?ar app.jar
COPY --chown=app:app secret.env .env


# Remove write permissions from app files for security
RUN chmod 544 /app/app.jar && chmod 444 /app/.env

USER app

ENTRYPOINT ["java", "-Xms2G", "-Xmx2G", "-Dspring.profiles.active=pi_nuc,metrics", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:+UseG1GC", "-jar", "app.jar"]