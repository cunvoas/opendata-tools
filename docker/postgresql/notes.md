sudo docker exec -ti pg_autmel /bin/bash

sudo useradd -u 5432 postgres
sudo groupadd -u 5432 postgres


userdel postgres

sudo groupadd postgres -g 5433 
sudo useradd postgres -u 5432 -g 5433 -m -s /bin/bash


CREATE ROLE mattermost WITH
	NOLOGIN
	NOSUPERUSER
	NOCREATEDB
	NOCREATEROLE
	INHERIT
	NOREPLICATION
	CONNECTION LIMIT -1
	PASSWORD 'mmUser2023*~';
CREATE DATABASE mattermost
    WITH
    OWNER = mattermost
    ENCODING = 'UTF8'
    LOCALE_PROVIDER = 'libc'
    CONNECTION LIMIT = -1
    IS_TEMPLATE = False;
