#!/bin/bash

# install java environment
apt-get update & apt install -y openjdk-16-jdk


# launch the app
./gradlew run