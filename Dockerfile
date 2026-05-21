FROM eclipse-temurin:21-jre

WORKDIR /app

COPY build/libs/TourneyBackend-all.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]