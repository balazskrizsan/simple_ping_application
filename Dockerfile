FROM openjdk:21-jdk-slim

WORKDIR /app

COPY /target/simple_ping_application-1.0-SNAPSHOT.jar /app/simple_ping_application.jar

EXPOSE 8080

CMD ["java", "-jar", "simple_ping_application.jar"]
