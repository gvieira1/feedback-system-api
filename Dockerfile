FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY target/feedback-0.0.1-SNAPSHOT.jar feedback.jar
ENTRYPOINT ["java", "-jar", "feedback.jar"]