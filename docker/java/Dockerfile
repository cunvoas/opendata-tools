#FROM gcr.io/distroless/java21-debian12
#FROM eclipse-temurin:21.0.9_10-jdk-alpine-3.23
#FROM bellsoft/liberica-runtime-container:jdk-21.0.9_12-crac-cds-slim-glibc
FROM docker pull bellsoft/liberica-openjdk-alpine-musl:25-cds
RUN apk --no-cache add curl

#FROM openjdk:25-rc-jdk-slim-trixie
#RUN apt-get update && apt-get install -y curl \
#   && apt-get clean \
#   && rm -rf /var/lib/apt/lists/*



COPY geoservice-isochrone-*.?ar app.jar 
COPY secret.env .env

ENTRYPOINT ["java", "-Xms2g", "-Xmx2g", "-Dspring.profiles.active=prod,pi,metrics", "-jar", "app.jar"]

