#!/bin/bash

GREEN="32"
BOLDGREEN="\e[1;${GREEN}m"
ENDCOLOR="\e[0m"

TAG=${TAG:-latest}

echo -e "${BOLDGREEN}Building server${ENDCOLOR}"
cd server
./gradlew dockerBuild
cd ..

echo -e "${BOLDGREEN}Building docker with frontend${ENDCOLOR}"
docker build . -t exacaster/lighter:${TAG} --network host
