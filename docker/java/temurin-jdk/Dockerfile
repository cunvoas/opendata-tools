FROM eclipse-temurin:21.0.10_7-jdk-alpine-3.22

RUN apk update
RUN apk --no-cache add curl

COPY geoservice-isochrone-*.?ar app.jar 
COPY secret.env .env

ENTRYPOINT ["java", "-Xms2G", "-Xmx2G", "-Dspring.profiles.active=pi_nuc,metrics", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:+UseG1GC", "-jar", "app.jar"]

