# ---- Stage 1: Build ----
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /workspace
RUN apt-get update && apt-get install -y git \
    && git clone --depth 1 https://github.com/aleks-r74/UniversalWebParser.git \
    && chmod +x UniversalWebParser/back-end/gradlew \
    && ./UniversalWebParser/back-end/gradlew -p UniversalWebParser/back-end clean bootJar --no-daemon

# ---- Stage 2: Runtime ----
FROM eclipse-temurin:21-jre-jammy

# Install git, curl, Node.js
RUN apt-get update && apt-get install -y curl \
    && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs \
    && npm install -g playwright \
    && npx playwright install --with-deps \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the JAR from builder
COPY --from=builder /workspace/UniversalWebParser/back-end/build/libs/uniparser.jar ./uniparser.jar

# Run the app
ENTRYPOINT ["java", "-jar", "uniparser.jar"]
