FROM openjdk:8-jdk-alpine

WORKDIR /backend

COPY . .

RUN ./gradlew build

ENTRYPOINT ["./gradlew", "run"]
