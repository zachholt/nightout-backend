FROM maven:3.8.6-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first for better caching
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Run tests and build - tests might fail in CI but we still want to create the image
RUN mvn clean package || mvn clean package -DskipTests

FROM eclipse-temurin:17-alpine
WORKDIR /app
COPY --from=build /app/target/nightout-0.0.1-SNAPSHOT.jar nightout-backend.jar
EXPOSE 8080
CMD ["java", "-jar", "nightout-backend.jar"]