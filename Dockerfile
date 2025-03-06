FROM eclipse-temurin:17-alpine
WORKDIR /app
COPY target/nightout-0.0.1-SNAPSHOT.jar nightout-backend.jar
EXPOSE 8080
CMD ["java", "-jar", "nightout-backend.jar"]