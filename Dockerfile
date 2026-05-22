# Stage 1: Build the fat JAR using Gradle
FROM gradle:8.8-jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN ./gradlew buildFatJar --no-daemon

# Stage 2: Create the lightweight runtime image
FROM eclipse-temurin:21-jre
EXPOSE 8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/TourneyBackend-all.jar /app/app.jar
WORKDIR /app
CMD ["java", "-jar", "app.jar"]