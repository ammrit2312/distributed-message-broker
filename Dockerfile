# This is a multi-stage Dockerfile for building and then running the Java application 

# Step 1: Build the application using Maven
FROM maven:3.9-eclipse-temurin-21 AS build 
WORKDIR /build
# Copy the pom.xml file and download dependencies
COPY pom.xml . 
# Download dependencies to the local Maven repository to speed up the build process
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B package -DskipTests

# Step 2: Create a lightweight image for running the application
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/target/distributed-message-broker-1.0-SNAPSHOT.jar broker.jar
EXPOSE 9001
ENTRYPOINT ["java", "-jar", "broker.jar"]