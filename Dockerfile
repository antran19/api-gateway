FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY libs libs
COPY platform platform
COPY services services
RUN mvn -q -pl platform/api-gateway -am -DskipTests package

FROM eclipse-temurin:21-jre
COPY --from=build /workspace/platform/api-gateway/target/api-gateway-0.1.0-SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
