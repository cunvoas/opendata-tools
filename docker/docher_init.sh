export VOLUMES_PATH=/mnt/usb1/ASSOS/docker/volumes

#export VOLUMES_PATH=/work/docker/volumes

mkdir -p $VOLUMES_PATH/data/pgsql/
mkdir -p $VOLUMES_PATH/log/pgsql/
mkdir -p $VOLUMES/log/java/
mkdir -p $VOLUMES_PATH/log/nginx/
mkdir -p $VOLUMES_PATH/log/admin/



exit 0

sudo docker volume create --name=vol_pgsql_data --opt type=none --opt device=$VOLUMES_PATH/data/pgsql/ --opt o=bind
mkdir -p $VOLUMES_PATH/volumes/log/pgsql/
sudo docker volume create --name=vol_pgsql_log --opt type=none --opt device=$VOLUMES_PATH/log/pgsql/ --opt o=bind

mkdir -p $VOLUMES/volumes/log/java/
sudo docker volume create --name=vol_java_log --opt type=none --opt device=$VOLUMES_PATH/log/java/ --opt o=bind

mkdir -p $VOLUMES_PATH/volumes/log/nginx/
sudo docker volume create --name=vol_nginx_log --opt type=none --opt device=$VOLUMES_PATH/log/nginx/ --opt o=bind

mkdir -p $VOLUMES_PATH/volumes/log/map/
sudo docker volume create --name=vol_map_log --opt type=none --opt device=$VOLUMES_PATH/log/map/ --opt o=bind

# https://stackoverflow.com/questions/49950326/how-to-create-docker-volume-device-host-path
#docker volume create --name pgsql_data --opt type=none --opt device=/home/jinna/Jinna_Balu/Test_volume --opt o=bind


# https://www.raspberrypi.com/documentation/computers/config_txt.html#disable_poe_fan

# https://devopssec.fr/article/fonctionnement-manipulation-reseau-docker


docker network create -d none --subnet=172.16.1.0/24 --gateway=172.16.1.254 autmel-reverse-proxy
docker network create -d none --subnet=172.16.2.0/24 --gateway=172.16.2.254 autmel-front
docker network create -d none --subnet=172.16.3.0/24 --gateway=172.16.3.254 autmel-backend
docker network create -d none --subnet=172.16.4.0/24 --gateway=172.16.4.254 autmel-data
