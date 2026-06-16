# syntax=docker/dockerfile:1

# --- Build stage: compile and package the Spring Boot layered jar ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Cache dependencies first: copy only the build descriptor, resolve, then add sources.
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q clean package -DskipTests

# Extract the layered jar so Docker can cache stable layers (deps) separately from app code.
RUN java -Djarmode=layertools -jar target/*.jar extract --destination extracted

# --- Runtime stage: JRE only, non-root, container-aware memory ---
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Run as an unprivileged user.
RUN addgroup -S app && adduser -S app -G app

# Copy layers from least to most frequently changing for optimal image-layer caching.
COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./

USER app

# Container-aware heap sizing keeps memory bounded by the container limit, not the host.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport -XX:+ExitOnOutOfMemoryError"

EXPOSE 8761
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
