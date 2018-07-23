FROM internal.docker.ospcfc.tech.lastmile.com/cfcdevops/alpine-java:jdk-8u131
MAINTAINER cfc-systems-reporting-xd@ocado.com

RUN mkdir -p /ocado/libs
RUN mkdir -p /ocado/server
RUN mkdir -p /ocado/results

ADD dist/gatling-test-drive/*.jar /ocado/libs/
ADD dist/gatling-test-drive/libs/*.jar /ocado/libs/
WORKDIR /ocado

CMD java -cp "libs/*" Main