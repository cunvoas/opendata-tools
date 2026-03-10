FROM dhi.io/nginx:1-alpine3.21
#FROM nginx:alpine3.22-slim

RUN apk --no-cache add curl

COPY ./nginx.conf /etc/nginx/
COPY ./nginx_conf/* /etc/nginx/conf.d/
COPY ./default/* /etc/nginx/html/
COPY ./default/img/* /etc/nginx/html/img/

# Prepare mount points for runtime cert/passwd volumes (managed by certbot container)
# These are placeholders; actual content comes from mounted volumes at runtime.
USER 0
RUN mkdir -p /etc/nginx/certs /etc/nginx/passwd \
    && chown -R nginx:nginx /etc/nginx/certs /etc/nginx/passwd \
    && chmod 750 /etc/nginx/certs /etc/nginx/passwd

# Run as non-root nginx user
USER nginx

EXPOSE 80
EXPOSE 443
