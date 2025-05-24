#Use official Maven image as the base image
FROM maven:3.8.6-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml to cache dependencies
COPY pom.xml .

# Download dependencies if changed
RUN mvn -B dependency:go-offline

# Copy the rest of the project and build
COPY src ./src
#RUN mvn -B package -DskipTests

# Create a minimal image
#FROM openjdk:17-slim

# Set working directory
#WORKDIR /app

# Copy JAR file from the build stage
# COPY --from=build /app/target/*-runner.jar ./app.jar

# Expose port 8080 to the outside world
EXPOSE 8080

# Command to run the application
CMD ["mvn", "quarkus:dev", "-Dquarkus.http.host=0.0.0.0"]
# CMD ["java", "-jar", "app.jar"]
