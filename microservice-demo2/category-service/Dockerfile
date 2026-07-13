FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY category-service/pom.xml category-service/
RUN mvn -f category-service/pom.xml dependency:resolve -B && mvn -f category-service/pom.xml dependency:resolve-plugins -B
COPY category-service/src category-service/src
RUN mvn -f category-service/pom.xml clean package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/category-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
