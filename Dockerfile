# Build the Spring Boot application.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY blog-app/pom.xml ./pom.xml
COPY blog-app/src ./src
RUN mvn -B -ntp clean verify

# Run the packaged application as a non-root user.
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN useradd --system --uid 10001 spring
COPY --from=build /workspace/target/*.jar /app/app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
