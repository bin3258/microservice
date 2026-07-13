FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY product-service/pom.xml product-service/
RUN mvn -f product-service/pom.xml dependency:resolve -B && mvn -f product-service/pom.xml dependency:resolve-plugins -B
COPY product-service/src product-service/src
RUN mvn -f product-service/pom.xml clean package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/product-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
