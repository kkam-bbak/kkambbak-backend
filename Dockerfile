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

RUN ./gradlew :kkambbak:bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring

COPY --from=builder /workspace/app/kkambbak/build/libs/*.jar app.jar

RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "/app/app.jar"]