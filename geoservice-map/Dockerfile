# get image
FROM owasp/modsecurity-crs:3.3.4-nginx-alpine-202304160904

# copy map app in docker nginx
# see https://v2.vuejs.org/v2/cookbook/dockerize-vuejs-app.html
COPY ./dist/* /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]