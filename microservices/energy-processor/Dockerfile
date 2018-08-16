FROM openjdk:8-jre
MAINTAINER Justin Nguyen <tuan3.nguyen@gmail.com>

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/energy/processor.jar"]

ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/energy/processor.jar