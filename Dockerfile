FROM openjdk:11.0.5-jre-stretch

ADD ["sources.list", "/etc/apt/"]

RUN apt update && apt install -y fping

ADD ["target/ip-test-ns-1-all.jar", "settings.properties", "/"]

CMD java -jar ip-test-ns-1-all.jar