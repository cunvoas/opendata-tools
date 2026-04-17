FROM gcr.io/distroless/java25-debian13

WORKDIR /app

# Distroless built-in nonroot user/group
COPY --chown=65532:65532 nada-*.?ar app.jar
COPY --chown=65532:65532 secret.env .env


# Remove write permissions from app files for security
RUN chmod 544 /app/app.jar && chmod 444 /app/.env

USER 65532:65532

ENTRYPOINT ["java", "-Xms384m", "-Xmx384m", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
