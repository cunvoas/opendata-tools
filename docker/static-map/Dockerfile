FROM flashspys/nginx-static

RUN apk update && apk upgrade

COPY ./dist/* /static/
COPY ./dist/css/* /static/css/
COPY ./dist/js/* /static/js/
COPY ./dist/img/* /static/img/

# Set non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

EXPOSE 81