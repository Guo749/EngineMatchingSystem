#!/bin/bash

# install java environment
apt update & apt install -y openjdk-16-jdk


# launch the app
./gradlew run