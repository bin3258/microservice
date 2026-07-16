FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY review-service/pom.xml review-service/
RUN mvn -f review-service/pom.xml dependency:resolve -B && mvn -f review-service/pom.xml dependency:resolve-plugins -B
COPY review-service/src review-service/src
RUN mvn -f review-service/pom.xml clean package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/review-service/target/*.jar app.jar
EXPOSE 8096
ENTRYPOINT ["java", "-jar", "app.jar"]
