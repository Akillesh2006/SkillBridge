# Multi-stage Dockerfile for SkillBridge Spring Boot Backend
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy dependency definition and source code
COPY pom.xml .
COPY src ./src

# Build production JAR skipping tests
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy built artifact from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose container port (Railway overrides PORT dynamically)
EXPOSE 8080

# Launch application
ENTRYPOINT ["java", "-jar", "app.jar"]
