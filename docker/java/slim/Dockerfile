FROM openjdk:25-rc-jdk-slim-trixie
RUN apk --no-cache add curl

COPY geoservice-isochrone-*.?ar app.jar 
COPY secret.env .env

ENTRYPOINT ["java", "-Xms2g", "-Xmx2g", "-Dspring.profiles.active=prod,pi,metrics", "-jar", "app.jar"]

