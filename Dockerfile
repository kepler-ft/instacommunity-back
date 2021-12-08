FROM openjdk:11-alpine

WORKDIR /backend

COPY . .

RUN ./gradlew build

ENTRYPOINT ["./gradlew", "run"]
