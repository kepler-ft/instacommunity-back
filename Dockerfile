FROM openjdk:11-slim-bullseye

WORKDIR /backend

COPY . .

RUN ./gradlew build

ENTRYPOINT ["./gradlew", "run"]
