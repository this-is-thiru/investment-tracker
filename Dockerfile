FROM openjdk:25-jdk

# Optional: Set a non-root work directory
WORKDIR /app

# Expose application port
EXPOSE 8080

# Copy the built jar into the image
# (COPY is preferred over ADD for local files)
COPY target/investment-tracker.jar /app/investment-tracker.jar

# Provide a default profile (can be overridden at runtime with -e SPRING_PROFILES_ACTIVE=prod)
ENV SPRING_PROFILES_ACTIVE=default

# Start the Spring Boot app; it will read SPRING_PROFILES_ACTIVE automatically
ENTRYPOINT ["java","-jar","/app/investment-tracker.jar"]

#FROM openjdk:25-jdk-slim
#EXPOSE 8080
#ADD target/investment-tracker.jar investment-tracker.jar
#ENTRYPOINT ["java","-jar","/investment-tracker.jar"]
#

## ---------- Build Stage ----------
#FROM maven:3.9.6-eclipse-temurin-21 AS build
#
#WORKDIR /app
#
## Copy the pom and source code
#COPY pom.xml .
#COPY src ./src
#
## Package the application
#RUN mvn clean package -DskipTests
#
## ---------- Runtime Stage ----------
#FROM eclipse-temurin:21-jdk
#
#WORKDIR /app
#
## Copy only the jar from the build stage
#COPY --from=build /app/target/investment-tracker.jar app.jar
#
#EXPOSE 8080
#
#ENTRYPOINT ["java", "-jar", "app.jar"]
