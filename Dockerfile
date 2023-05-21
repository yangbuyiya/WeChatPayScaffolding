FROM anapsix/alpine-java:8_server-jre_unlimited

MAINTAINER Yang Shuai

RUN mkdir -p /root/jpom/docker/server/logs \
    /root/jpom/docker/server/temp

WORKDIR /root/jpom/docker/server

ENV SERVER_PORT=9080

EXPOSE ${SERVER_PORT}

ADD ./target/wx-play-demo.jar ./app.jar

ENTRYPOINT ["java", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-Dserver.port=${SERVER_PORT}", \
            "-jar", "app.jar"]
