FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY Search-service/pom.xml Search-service/
RUN mvn -f Search-service/pom.xml dependency:resolve -B && mvn -f Search-service/pom.xml dependency:resolve-plugins -B
COPY Search-service/src Search-service/src
RUN mvn -f Search-service/pom.xml clean package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/Search-service/target/*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
