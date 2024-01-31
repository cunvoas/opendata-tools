FROM --platform=linux/arm64 kartoza/postgis:15-3.4--v2023.08.20

#RUN groupadd postgres -g 5433 
#RUN useradd postgres -u 5432 -g 5433 -m -s /bin/bash

ENV PGDATA /var/postgresql/data
# this 777 will be replaced by 700 at runtime (allows semi-arbitrary "--user" values)
RUN mkdir -p "$PGDATA" && chown -R postgres:postgres "$PGDATA" && chmod 777 "$PGDATA"
VOLUME /var/postgresql/data
RUN chown -R postgres:postgres "$PGDATA" && chmod 777 "$PGDATA"
