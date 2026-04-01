FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY target/VMS-EUREKA-SERVER-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-Xms64m", "-Xmx180m", "-jar", "app.jar"]