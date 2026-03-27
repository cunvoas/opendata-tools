FROM gcr.io/distroless/java25-debian13

WORKDIR /app

# Distroless built-in nonroot user/group
COPY --chown=65532:65532 geoservice-isochrone-*.?ar app.jar
COPY --chown=65532:65532 secret.env .env


# Remove write permissions from app files for security
RUN chmod 544 /app/app.jar && chmod 444 /app/.env

USER 65532:65532

ENTRYPOINT ["java", "-Xms2g", "-Xmx2g", "-Dspring.profiles.active=prod,pi,metrics", "-jar", "app.jar"]