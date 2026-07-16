FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY notification-service/pom.xml notification-service/
RUN mvn -f notification-service/pom.xml dependency:resolve -B && mvn -f notification-service/pom.xml dependency:resolve-plugins -B
COPY notification-service/src notification-service/src
RUN mvn -f notification-service/pom.xml clean package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/notification-service/target/*.jar app.jar
EXPOSE 8093
ENTRYPOINT ["java", "-jar", "app.jar"]
