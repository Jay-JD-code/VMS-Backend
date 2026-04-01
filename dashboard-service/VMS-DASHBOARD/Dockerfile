FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY target/VMS-DASHBOARD-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-Xms64m", "-Xmx180m", "-jar", "app.jar"]