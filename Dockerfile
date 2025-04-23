# Build stage
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Deploy stage
FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY --from=build /app/target/kafka-schema-registry-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]