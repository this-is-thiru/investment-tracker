# ---------- Build Stage ----------
FROM maven:3.9.8-eclipse-temurin-25 AS build
WORKDIR /app

# Copy the pom and source code
COPY pom.xml .
COPY src ./src

# Package the application
RUN mvn clean install -DskipTests

# ---------- Runtime Stage ----------
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/investment-tracker.jar /app.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=default
ENTRYPOINT ["java", "-jar", "/app.jar"]
