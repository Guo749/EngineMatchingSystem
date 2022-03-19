FROM ubuntu

LABEL description="Engine Docker"

RUN apt-get update && apt install -y curl net-tools debconf-utils tzdata

COPY . /code

RUN chmod 777 /code/init.sh

WORKDIR /code

ENTRYPOINT ["/code/init.sh"]
