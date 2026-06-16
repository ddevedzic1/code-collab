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

# --- Runtime stage: JRE + docker CLI, container-aware memory ---
# This service shells out to `docker run` (DockerExecutionRunner) against the
# host Docker daemon mounted at /var/run/docker.sock, so the docker CLI must be
# present. It runs as root so it can always read the mounted socket regardless
# of the host socket's group id; the sandboxed user code itself still runs in a
# locked-down child container (--network none, --read-only, non-root, limits).
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN apk add --no-cache docker-cli

# Copy layers from least to most frequently changing for optimal image-layer caching.
COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./

# Container-aware heap sizing keeps memory bounded by the container limit, not the host.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport -XX:+ExitOnOutOfMemoryError"

EXPOSE 8083
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
