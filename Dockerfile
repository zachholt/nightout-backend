FROM eclipse-temurin:17-alpine
WORKDIR /app
COPY target/nightoutbackend-0.0.1-SNAPSHOT.jar nightoutbackend.jar
EXPOSE 8080
CMD ["java", "-jar", "nightoutbackend.jar"]