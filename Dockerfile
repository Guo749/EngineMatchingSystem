FROM openjdk:16-alpine3.13

COPY . /code

RUN chmod 777 /code/init.sh

WORKDIR /code

ENTRYPOINT ["sh", "/code/init.sh"]
