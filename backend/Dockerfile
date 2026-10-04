FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY scripts/maven-retry.sh /usr/local/bin/health-center-maven
RUN sh /usr/local/bin/health-center-maven -DskipTests dependency:go-offline

COPY src ./src
RUN sh /usr/local/bin/health-center-maven -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=5s --start-period=90s --retries=6 \
    CMD curl --fail --silent http://127.0.0.1:8080/actuator/health > /dev/null || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
