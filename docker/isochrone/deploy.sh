#!/bin/sh

sudo docker compose build
sudo docker compose down
sudo docker compose up -d

sudo docker  exec   autmel_isochrone mkdir -p /var/isochrone/mail/mailjet
sudo docker cp ./mailjet/logo-autmel.png autmel_isochrone:/var/isochrone/mail/mailjet/logo-autmel.png
