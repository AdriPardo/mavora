# Build
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace
COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true
COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

# Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S mavora && adduser -S mavora -G mavora \
  && mkdir -p /app/data/media && chown -R mavora:mavora /app
COPY --from=build /workspace/build/libs/mavora-*.jar /app/app.jar
USER mavora
EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=5s --retries=10 --start-period=60s \
  CMD wget -qO- http://127.0.0.1:8080/actuator/health/liveness || exit 1
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
