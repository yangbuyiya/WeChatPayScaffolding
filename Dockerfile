FROM anapsix/alpine-java:8_server-jre_unlimited

MAINTAINER Yang Shuai

RUN mkdir -p /root/jpom/docker/server/wx-play-demo/logs \
    /root/jpom/docker/server/wx-play-demo/temp

WORKDIR /root/jpom/docker/server/wx-play-demo

ENV SERVER_PORT=9080

EXPOSE ${SERVER_PORT}

ADD ./target/wx-play-demo.jar ./app.jar

ENTRYPOINT ["java", "-Dserver.port=${SERVER_PORT}", "-jar", "app.jar"]
