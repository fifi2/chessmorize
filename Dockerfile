FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# to debug FROM eclipse-temurin:21-jre
FROM gcr.io/distroless/java21-debian12:nonroot
WORKDIR /app
COPY --from=build /app/target/*.jar chessmorize.jar
ENTRYPOINT ["java", "-jar", "chessmorize.jar"]
