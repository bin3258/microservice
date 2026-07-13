FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY Cart-service/pom.xml Cart-service/
RUN mvn -f Cart-service/pom.xml dependency:resolve -B && mvn -f Cart-service/pom.xml dependency:resolve-plugins -B
COPY Cart-service/src Cart-service/src
RUN mvn -f Cart-service/pom.xml clean package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/Cart-service/target/*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
