# ---------- Build Stage ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
RUN apt-get update && apt-get install -y maven

# Copy the pom and source code
COPY pom.xml .
COPY src ./src

# Package the application
RUN mvn clean install -DskipTests

# ---------- Runtime Stage ----------
FROM eclipse-temurin:25-jdk
WORKDIR /app

# Copy only the jar from the build stage
COPY --from=build /app/target/investment-tracker.jar investment-tracker.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=default
ENTRYPOINT ["java", "-jar", "investment-tracker.jar"]
