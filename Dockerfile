# syntax=docker/dockerfile:1.7
FROM gradle:9.1.0-jdk21 AS build
WORKDIR /workspace

COPY settings.gradle.kts build.gradle.kts ./
COPY src ./src

RUN gradle --no-daemon clean bootJar

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 eshop

COPY --from=build /workspace/build/libs/eshop-backend.jar /app/eshop-backend.jar

USER eshop
EXPOSE 8282
HEALTHCHECK --interval=15s --timeout=5s --retries=20 CMD curl -fsS http://localhost:8282/actuator/health | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+UseG1GC", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/eshop-backend.jar"]
