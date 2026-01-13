FROM maven:3.8.4-openjdk-17 AS build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package

FROM eclipse-temurin:25-jdk

EXPOSE 8080

COPY --from=build /app/target/*.jar  /home/xcai/app.jar
WORKDIR /home/xcai

ENTRYPOINT ["java","-Dfile.encoding=UTF-8","-jar","/home/xcai/app.jar"]
