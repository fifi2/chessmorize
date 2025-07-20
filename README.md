# Chessmorize

Chessmorize is a web application to memorize and train on chess studies imported
from Lichess. It allows you to manage "books" (repertoires), organize them into
chapters and lines, and train according to a smart calendar.

## Main Features

- Import books from Lichess (using a study ID).
- Parse and store PGN files retrieved from Lichess.
- Flatten the study into trainable lines.
- Planned: handle transpositions in the lines to avoid repetition in the
  upstream study.
- Manage training lines.
- Enable/disable chapters (useful to ignore introduction chapters for example).
- Daily training based on a configurable calendar, with spaced repetition
  algorithm.
- RESTful API.
- Planned: web interface (Thymeleaf).

## Technical Stack

- Java 21
- Maven 3.9+
- Docker
- H2 database (file mode, R2DBC)

## Quick Start

1. **Build and run the Docker image**

   ```shell
   docker compose up --build
   ```

See the `docker-compose.yml` file for details on the Docker setup.

2. Access the [Chessmorize interface](http://localhost:8080).

## Configuration

TODO: Describe where the database is stored, and how to persist it across the
Docker container lifecycle.

## License

Personal project, open-source.
