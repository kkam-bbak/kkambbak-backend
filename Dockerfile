# API 서버 이미지
FROM eclipse-temurin:21-jre-jammy AS api

# FFMPEG 및 HTTPS 필수 라이브러리 설치
RUN apt-get update && apt-get install -y --no-install-recommends \
    ffmpeg \
    ca-certificates && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY kkambbak/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Duser.timezone=Asia/Seoul", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "/app/app.jar"]

# 스케줄러 이미지
FROM eclipse-temurin:21-jre-jammy AS scheduler

WORKDIR /app

COPY kkambbak-scheduler/build/libs/*.jar app.jar

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Duser.timezone=Asia/Seoul", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "/app/app.jar"]