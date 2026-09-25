FROM eclipse-temurin:21-jre
COPY target/api-gateway-0.1.0-SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
