FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/
RUN mvn -f eureka-server/pom.xml dependency:resolve -B && mvn -f eureka-server/pom.xml dependency:resolve-plugins -B
COPY eureka-server/src eureka-server/src
RUN mvn -f eureka-server/pom.xml clean package -DskipTests -B

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/eureka-server/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
