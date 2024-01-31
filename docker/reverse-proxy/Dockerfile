FROM nginx:alpine3.18-slim

COPY ./nginx.conf /etc/nginx/
COPY ./nginx_conf/* /etc/nginx/conf.d/

COPY ./default/* /etc/nginx/html/
COPY ./default/img/* /etc/nginx/html/img/

# Set non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

EXPOSE 80
EXPOSE 443
