FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY settings.gradle.kts build.gradle.kts gradle.properties ./
RUN chmod +x gradlew

COPY src ./src
RUN ./gradlew --no-daemon installDist

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd -r -u 1001 -m appuser
COPY --from=build /app/build/install/studytracker-backend ./

USER appuser
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"

ENTRYPOINT ["./bin/studytracker-backend"]
