# DEVELOPERS.md

## Architecture

- **Main stack**: Java 21, Spring Boot (WebFlux, R2DBC, Thymeleaf), H2 (file
  mode, R2DBC), Maven
- **Organization**:
    - `controller/api`: REST endpoints (API contract)
    - `controller/www`: endpoints for HTML rendering (Thymeleaf)
    - `service`: business logic (book management, training, etc.)
    - `model`: domain entities (Book, Chapter, Line...)
    - `repository`: database access (R2DBC)
    - `config`: Spring configuration, WebClient, etc.
    - `error`: exception handling
    - `converter`: format conversion (e.g., PGN)

## Best Practices

- Respect separation of API / business logic / persistence
- REST endpoints are unit tested (contract, errors, retry)
- Services are unit tested (business logic)
- Integration tests (IT) are in `it/` and split by use-case
- Use Spring profiles (`test`, `default`, etc.)
- Use JUnit extensions for specific mocks (e.g., Lichess)

## Local Start (development)

1. Compile and run with Maven:

   ```shell
   mvn spring-boot:run
   ```

2. You can now access:

    - [API](http://localhost:8080/api)
    - [OpenAPI documentation](http://localhost:8080/swagger-ui.html)
    - [UI](http://localhost:8080)

## Tests

- Integration tests use an isolated H2 database
- Mocks (e.g., Lichess) are injected via JUnit 5 extensions

## Deployment

Use the distroless Dockerfile for production.

```shell
docker compose up --build
```

## Contribution

- Fork, create a branch, PR
- Follow code organization and existing conventions
- Add tests for any new feature or fix
