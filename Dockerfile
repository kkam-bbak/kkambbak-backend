FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /workspace/app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

COPY kkambbak/build.gradle kkambbak/build.gradle
COPY kkambbak-scheduler/build.gradle kkambbak-scheduler/build.gradle
COPY kkambbak-core/build.gradle kkambbak-core/build.gradle
COPY kkambbak-client/build.gradle kkambbak-client/build.gradle

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew dependencies --no-daemon || true

COPY kkambbak/src kkambbak/src
COPY kkambbak-scheduler/src kkambbak-scheduler/src
COPY kkambbak-core/src kkambbak-core/src
COPY kkambbak-client/src kkambbak-client/src

RUN --mount=type=cache,target=/root/.gradle \
    --mount=type=cache,target=/workspace/app/build \
    ./gradlew build --no-daemon

# API 서버 이미지
FROM eclipse-temurin:21-jre-jammy AS api

WORKDIR /app

COPY --from=builder /workspace/app/kkambbak/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "/app/app.jar"]

# 스케줄러 이미지
FROM eclipse-temurin:21-jre-jammy AS scheduler

WORKDIR /app

COPY --from=builder /workspace/app/kkambbak-scheduler/build/libs/*.jar app.jar

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "/app/app.jar"]