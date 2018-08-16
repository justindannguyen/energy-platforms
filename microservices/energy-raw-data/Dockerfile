FROM openjdk:8-jre
LABEL maintainer Justin Nguyen <tuan3.nguyen@gmail.com>
EXPOSE 8080
ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/energy/raw-data.jar"]

ARG JAR_FILE
ADD target/${JAR_FILE} /usr/share/energy/raw-data.jar