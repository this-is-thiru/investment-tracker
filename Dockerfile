FROM openjdk:25-jdk
EXPOSE 8080
ADD target/investment-tracker.jar investment-tracker.jar
ENTRYPOINT ["java","-jar","/investment-tracker.jar"]

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
