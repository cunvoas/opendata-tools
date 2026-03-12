FROM pilotfishtechnology/eiplatform:23R1-openjdk-hardened

# Create dedicated runtime user for least-privilege execution
RUN addgroup -S app && adduser -S -G app app

WORKDIR /app

COPY --chown=app:app geoservice-isochrone-*.?ar app.jar
COPY --chown=app:app secret.env .env

# Remove write permissions from app files for security
RUN chmod 544 /app/app.jar && chmod 444 /app/.env

USER app

ENTRYPOINT ["java", "-Xms2g", "-Xmx2g", "-Dspring.profiles.active=prod,pi,metrics", "-jar", "app.jar"]