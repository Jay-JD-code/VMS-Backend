FROM openjdk:21-jdk-slim
WORKDIR /app
COPY target/VMS-DASHBOARD-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]