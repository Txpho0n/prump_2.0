# Базовый образ с Java 17
FROM openjdk:17-jdk-slim

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файлы сборки Maven
COPY pom.xml .
COPY src ./src


# Устанавливаем Maven и собираем проект
RUN apt-get update && apt-get install -y maven && mvn clean package

# Копируем собранный JAR-файл
COPY --from=builder /app/target/algo_train.jar app.jar


# Команда для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
