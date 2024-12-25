FROM openjdk:21-jdk

COPY .. .
RUN mvn clean install

EXPOSE 8080
ADD target/investment-tracker.jar investment-tracker.jar
ENTRYPOINT ["java","-jar","/investment-tracker.jar"]