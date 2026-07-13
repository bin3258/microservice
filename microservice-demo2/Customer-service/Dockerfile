FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY Customer-service/pom.xml Customer-service/
RUN mvn -f Customer-service/pom.xml dependency:resolve -B && mvn -f Customer-service/pom.xml dependency:resolve-plugins -B
COPY Customer-service/src Customer-service/src
RUN mvn -f Customer-service/pom.xml clean package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/Customer-service/target/*.jar app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "app.jar"]
