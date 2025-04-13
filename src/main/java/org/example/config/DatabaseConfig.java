package org.example.config;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static DatabaseConfig instance;
    private final Connection connection;

    private DatabaseConfig() {
        String url = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : "jdbc:postgresql://localhost:5432/cu_mock";
        String user = System.getenv("DB_USERNAME") != null ? System.getenv("DB_USERNAME") : "postgres";
        String password = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "postgres";

        // Настройка Flyway
        logger.info("Configuring Flyway with URL: {}", url);
        Flyway flyway = Flyway.configure()
                .dataSource(url, user, password)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();

        // Ожидание базы и миграция
        int retries = 15;
        int delayMs = 3000;
        boolean migrated = false;

        while (!migrated && retries > 0) {
            try {
                logger.info("Starting Flyway migration (retries left: {})", retries);
                flyway.migrate();
                logger.info("Flyway migration completed");
                migrated = true;
            } catch (Exception e) {
                retries--;
                logger.warn("Flyway migration failed: {}. Retrying in {}ms ({} retries left)", e.getMessage(), delayMs, retries);
                if (retries == 0) {
                    logger.error("Could not complete Flyway migration after all retries", e);
                    throw new RuntimeException("Flyway migration failed", e);
                }
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during Flyway retry", ie);
                }
            }
        }

        // Подключение к базе
        try {
            logger.info("Connecting to database: {}", url);
            this.connection = DriverManager.getConnection(url, user, password);
            logger.info("Database connection established");
        } catch (SQLException e) {
            logger.error("Failed to connect to database", e);
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    public static DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}