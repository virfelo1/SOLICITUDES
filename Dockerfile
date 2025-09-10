FROM gradle:8.14.3-jdk17-alpine AS build
WORKDIR /workspace
COPY . .
# Build only the reactive web entry point as executable jar; skip tests for faster image builds
RUN ./gradlew :app-service:bootJar --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom"
# Copy the generated Spring Boot fat jar
COPY --from=build /workspace/applications/app-service/build/libs/*.jar SOLICITUDES.jar
# Create non-root user
RUN addgroup -S app && adduser -S app -G app
USER app
EXPOSE 8081
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar SOLICITUDES.jar" ]
