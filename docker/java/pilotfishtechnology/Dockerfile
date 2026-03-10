FROM pilotfishtechnology/eiplatform:23R1-openjdk-hardened

# Create dedicated runtime user for least-privilege execution
RUN addgroup -S app && adduser -S -G app app

WORKDIR /app

COPY --chown=app:app geoservice-isochrone-*.?ar app.jar
COPY --chown=app:app secret.env .env

USER app

ENTRYPOINT ["java", "-Xms2g", "-Xmx2g", "-Dspring.profiles.active=prod,pi,metrics", "-jar", "app.jar"]