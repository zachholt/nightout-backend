#!/bin/bash

# Run Spring Boot app with local profile
export SPRING_PROFILES_ACTIVE=local
./mvnw spring-boot:run 