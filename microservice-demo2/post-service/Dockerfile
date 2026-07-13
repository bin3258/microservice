FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY post-service/pom.xml post-service/
RUN mvn -f post-service/pom.xml dependency:resolve -B && mvn -f post-service/pom.xml dependency:resolve-plugins -B
COPY post-service/src post-service/src
RUN mvn -f post-service/pom.xml clean package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/post-service/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
