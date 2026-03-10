FROM eclipse-temurin:21.0.10_7-jdk-alpine-3.22
RUN apk --no-cache add curl \
    && addgroup -S app \
    && adduser -S -G app app

WORKDIR /app

COPY --chown=app:app geoservice-isochrone-*.?ar app.jar
COPY --chown=app:app secret.env .env


USER app


#ENTRYPOINT ["java", "-Dcom.sun.management.jmxremote","-Dcom.sun.management.jmxremote.port=9010","-Dcom.sun.management.jmxremote.rmi.port=9010","-Dcom.sun.management.jmxremote.local.only=true","-Dcom.sun.management.jmxremote.authenticate=false","-Dcom.sun.management.jmxremote.ssl=false","-Dspring.profiles.active=prod,pi", "-jar", "app.jar"]

ENTRYPOINT ["java", "-Xms2G", "-Xmx2G", "-Dspring.profiles.active=pi_nuc,metrics", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:+UseG1GC", "-jar", "app.jar"]