#FROM gcr.io/distroless/java21-debian12


FROM eclipse-temurin:21.0.9_10-jdk-alpine-3.23
RUN apk update
RUN apk --no-cache add curl

#FROM openjdk:25-rc-jdk-slim-trixie
#RUN apt-get update && apt-get install -y curl
#RUN apt-get upgrade -y



COPY geoservice-isochrone-*.?ar app.jar 
COPY secret.env .env


#RUN addgroup --system --gid 1001 appuser 
#RUN adduser --system --uid 1001 --gid 1001 appuser
#USER 1001


#ENTRYPOINT ["java", "-Dcom.sun.management.jmxremote","-Dcom.sun.management.jmxremote.port=9010","-Dcom.sun.management.jmxremote.rmi.port=9010","-Dcom.sun.management.jmxremote.local.only=true","-Dcom.sun.management.jmxremote.authenticate=false","-Dcom.sun.management.jmxremote.ssl=false","-Dspring.profiles.active=prod,pi", "-jar", "app.jar"]

ENTRYPOINT ["java", "-Xms2G", "-Xmx2G", "-Dspring.profiles.active=pi_nuc,metrics", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:+UseG1GC", "-jar", "app.jar"]

