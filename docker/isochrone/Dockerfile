FROM openjdk:21-jdk-slim

RUN apt-get update && apt-get install -y curl
# TODO a decommenter en prod
#RUN apt-get upgrade -y

COPY geoservice-isochrone-*.?ar app.jar 
COPY secret.env .env

#RUN groupadd --gid 1000 node && useradd --uid 1000 --gid node --shell /bin/bash --create-home node
# Switch to 'appuser'
#USER 1000


ENTRYPOINT ["java", "-Dspring.profiles.active=prod,pi", "-jar", "app.jar"]