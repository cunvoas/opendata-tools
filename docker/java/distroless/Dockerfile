FROM gcr.io/distroless/java21-debian12

COPY geoservice-isochrone-*.?ar app.jar 
COPY secret.env .env

ENTRYPOINT ["java", "-Xms2g", "-Xmx2g", "-Dspring.profiles.active=prod,pi,metrics", "-jar", "app.jar"]

