sudo docker volume create --name postgres_data --driver local --opt type=nfs  --opt device=/your_ssd_drive/docker/volumes/postgresql


sudo docker volume create -name postgres_data --driver local --opt type=btrfs  --opt device=/mnt/usb1/ASSOS/docker/volumes/postgresql/



btrfs


https://ioflood.com/blog/docker-compose-volumes-how-to-mount-volumes-in-docker/