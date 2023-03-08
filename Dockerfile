FROM openjdk:11-jre-slim

COPY ./mondoc.jar /

ENTRYPOINT ["java", "-jar", "/mondoc.jar"]
