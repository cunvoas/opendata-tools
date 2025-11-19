# https://github.com/GoogleContainerTools/distroless/blob/main/java/README.md
#FROM gcr.io/distroless/java21-debian12
FROM docker.io/bellsoft/hardened-liberica-runtime-container:jdk-25-musl

COPY target/*.jar $USER_HOME/app.jar
