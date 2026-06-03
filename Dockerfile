# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace

COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle

RUN chmod +x ./gradlew

COPY src ./src

RUN ./gradlew --no-daemon clean bootJar

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --home-dir /app --shell /usr/sbin/nologin eshop \
    && chown -R eshop:eshop /app

COPY --from=build --chown=eshop:eshop /workspace/build/libs/eshop-backend.jar /app/eshop-backend.jar

USER eshop

EXPOSE 8282

HEALTHCHECK --interval=15s --timeout=5s --retries=20 --start-period=40s \
  CMD curl -fsS http://localhost:8282/actuator/health/readiness | grep -q '"status":"UP"' || exit 1

ENTRYPOINT [
  "java",
  "-XX:MaxRAMPercentage=75.0",
  "-XX:+UseG1GC",
  "-XX:+ExitOnOutOfMemoryError",
  "-Djava.security.egd=file:/dev/./urandom",
  "-jar",
  "/app/eshop-backend.jar"
]