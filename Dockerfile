FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests
RUN ls -l /app/target/  # Для отладки

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/algo_train.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
