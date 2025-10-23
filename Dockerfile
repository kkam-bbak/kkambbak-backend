FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /workspace/app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

COPY kkambbak/build.gradle kkambbak/build.gradle
COPY kkambbak-core/build.gradle kkambbak-core/build.gradle
COPY kkambbak-client/build.gradle kkambbak-client/build.gradle

COPY kkambbak/src kkambbak/src
COPY kkambbak-core/src kkambbak-core/src
COPY kkambbak-client/src kkambbak-client/src

RUN ./gradlew :kkambbak:build --no-daemon

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=builder /workspace/app/kkambbak/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "/app/app.jar"]