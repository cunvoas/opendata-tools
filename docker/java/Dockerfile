FROM openjdk:25-jdk-slim-bullseye
RUN apt-get update && apt-get install -y curl \
   && apt-get clean \
   && rm -rf /var/lib/apt/lists/*

#FROM gcr.io/distroless/java21-debian12
#RUN apk --no-cache add curl

COPY geoservice-isochrone-*.?ar app.jar 
COPY secret.env .env

ENTRYPOINT ["java", "-Xms512m", "-Xmx512m", "-Dspring.profiles.active=prod,pi,metrics", "-jar", "app.jar"]

