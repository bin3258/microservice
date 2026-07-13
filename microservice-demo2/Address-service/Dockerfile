FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY Address-service/pom.xml Address-service/
RUN mvn -f Address-service/pom.xml dependency:resolve -B && mvn -f Address-service/pom.xml dependency:resolve-plugins -B
COPY Address-service/src Address-service/src
RUN mvn -f Address-service/pom.xml clean package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/Address-service/target/*.jar app.jar
EXPOSE 8092
ENTRYPOINT ["java", "-jar", "app.jar"]
