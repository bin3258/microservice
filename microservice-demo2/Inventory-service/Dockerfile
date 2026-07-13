FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY Inventory-service/pom.xml Inventory-service/
RUN mvn -f Inventory-service/pom.xml dependency:resolve -B && mvn -f Inventory-service/pom.xml dependency:resolve-plugins -B
COPY Inventory-service/src Inventory-service/src
RUN mvn -f Inventory-service/pom.xml clean package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/Inventory-service/target/*.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
