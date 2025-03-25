# NightOut Backend

Backend service for the NightOut application, helping users plan their night out and discover venues.

## Running Locally

This project can be run in two different environments:

### Local Development Mode

The local development environment uses an H2 in-memory database instead of PostgreSQL, making it easy to run without external dependencies.

```shell
# Run with local profile
./run-local.sh
```

This will:
- Start the application with the "local" Spring profile
- Set up an H2 in-memory database
- Enable the H2 console at http://localhost:8081/h2-console
- Configure mock AI responses for local testing

### Production Mode

```shell
# Start the Docker containers as usual
docker-compose up -d
```

## API Documentation

When running locally, the Swagger UI is available at:
- http://localhost:8081/swagger-ui.html

In production, it's available at:
- http://44.203.161.109:8080/swagger-ui.html

## Testing and Code Coverage

You can run the tests with code coverage reporting using:

```shell
# Run tests with coverage
./run-tests.sh
```

This will:
- Run all tests with the JaCoCo code coverage tool
- Generate a coverage report at `target/site/jacoco/index.html`
- Automatically open the report in your browser

### Coverage Requirements

The project is configured to require a minimum of 70% line coverage. This threshold can be adjusted in the `pom.xml` file.

## H2 Console Access

When running in local mode, you can access the H2 database console at:
- http://localhost:8081/h2-console

Connection details:
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

## Available Profiles

- `prod`: Production profile with PostgreSQL database
- `local`: Local development profile with H2 database
- `test`: Testing profile with H2 database

You can set the profile manually using:
```
export SPRING_PROFILES_ACTIVE=local
```

## System Requirements

- Java 17+
- Maven 3.6+ 