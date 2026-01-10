FROM dhi.io/nginx:1-alpine3.21
#FROM nginx:alpine3.22-slim

RUN apk --no-cache add curl

COPY ./nginx.conf /etc/nginx/
COPY ./nginx_conf/* /etc/nginx/conf.d/

USER 0
RUN mkdir -p /etc/nginx/passwd/
USER $CONTAINER_USER_ID
COPY ./nginx_passwd/* /etc/nginx/passwd/

COPY ./default/* /etc/nginx/html/
COPY ./default/img/* /etc/nginx/html/img/

# Set non-root user
#RUN addgroup -S appgroup && adduser -S appuser -G appgroup
#USER appuser

EXPOSE 80
EXPOSE 443
