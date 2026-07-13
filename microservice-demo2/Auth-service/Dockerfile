FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY Auth-service/pom.xml Auth-service/
RUN mvn -f Auth-service/pom.xml dependency:resolve -B && mvn -f Auth-service/pom.xml dependency:resolve-plugins -B
COPY Auth-service/src Auth-service/src
RUN mvn -f Auth-service/pom.xml clean package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/Auth-service/target/*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
