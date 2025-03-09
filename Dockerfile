FROM maven:3.8.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-alpine
WORKDIR /app
COPY --from=build /app/target/nightout-0.0.1-SNAPSHOT.jar nightout-backend.jar
EXPOSE 8080
CMD ["java", "-jar", "nightout-backend.jar"]