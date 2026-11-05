FROM bellsoft/liberica-openjdk-alpine-musl:27-cds@sha256:ef4f0963921e907a66f8a6d2baebc56c33e075f82afb2c38156acb9e5f17c7a2

RUN apk update && apk --no-cache add curl \
    && addgroup -S app \
    && adduser -S -G app app

WORKDIR /app

COPY --chown=app:app geoservice-isochrone-*.?ar app.jar
COPY --chown=app:app secret.env .env

# Remove write permissions from app files for security
RUN chmod 544 /app/app.jar && chmod 444 /app/.env

USER app:app

EXPOSE 8089

ENTRYPOINT ["java", "-Xms2g", "-Xmx2g", "-Dspring.profiles.active=prod,pi,metrics", "-jar", "app.jar"]
