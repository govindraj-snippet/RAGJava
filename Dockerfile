# Stage 1: Build the application using Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the fat JAR using maven-assembly-plugin
RUN mvn clean compile assembly:single

# Stage 2: Create a lightweight runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the fat JAR from the build stage
COPY --from=build /app/target/VectorDB-Lite-1.0-SNAPSHOT-jar-with-dependencies.jar ./ragjava.jar

# Create a volume for local document mapping
VOLUME /docs

# Set the entrypoint to run the CLI application interactively
ENTRYPOINT ["java", "-jar", "ragjava.jar"]
