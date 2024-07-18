#!/bin/sh


source secret.env

mkdir -p ${VOLUMES_PATH}/mattermost/config
mkdir -p ${VOLUMES_PATH}/mattermost/data
mkdir -p ${VOLUMES_PATH}/mattermost/logs
mkdir -p ${VOLUMES_PATH}/mattermost/plugins
mkdir -p ${VOLUMES_PATH}/mattermost/client-plugins

sudo docker compose build
sudo docker compose down
sudo docker compose up -d

