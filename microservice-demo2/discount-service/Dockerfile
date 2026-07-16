FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY discount-service/pom.xml discount-service/
RUN mvn -f discount-service/pom.xml dependency:resolve -B && mvn -f discount-service/pom.xml dependency:resolve-plugins -B
COPY discount-service/src discount-service/src
RUN mvn -f discount-service/pom.xml clean package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/discount-service/target/*.jar app.jar
EXPOSE 8095
ENTRYPOINT ["java", "-jar", "app.jar"]
