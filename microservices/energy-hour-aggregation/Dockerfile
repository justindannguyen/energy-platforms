FROM openjdk:8-jre
LABEL maintainer Justin Nguyen <tuan3.nguyen@gmail.com>

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/energy/hour-aggregator.jar"]

ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/energy/hour-aggregator.jar