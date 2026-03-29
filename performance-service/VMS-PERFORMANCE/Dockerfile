FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/VMS-PERFORMANCE-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]