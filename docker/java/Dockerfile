FROM eclipse-temurin:21.0.9_10-jdk-alpine-3.23
RUN apk --no-cache add curl

#FROM openjdk:25-rc-jdk-slim-trixie
#RUN apt-get update && apt-get install -y curl \
#   && apt-get clean \
#   && rm -rf /var/lib/apt/lists/*

#FROM gcr.io/distroless/java21-debian12

COPY geoservice-isochrone-*.?ar app.jar 
COPY secret.env .env

ENTRYPOINT ["java", "-Xms2g", "-Xmx2g", "-Dspring.profiles.active=prod,pi,metrics", "-jar", "app.jar"]

