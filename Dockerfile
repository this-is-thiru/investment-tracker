FROM openjdk:21-jdk
EXPOSE 8080
ADD target/investment-tracker.jar investment-tracker.jar
ENTRYPOINT ["java","-jar","/investment-tracker.jar"]