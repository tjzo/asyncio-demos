#/bin/bash

while :
do
  docker stats --no-stream $1 | sed '1d'
sleep 2
done
