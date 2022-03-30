#!/bin/bash

echo "1 rebuild the docker, 0 not, default 0"

ARG1=${1:-0}

if [ $ARG1 -eq 1 ];then
  echo "rebuild it"
  sudo docker-compose up --build
else
  echo "using old ones"
  sudo docker-compose up
fi
