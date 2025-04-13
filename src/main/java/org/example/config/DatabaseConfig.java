package org.example.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static DatabaseConfig instance;
    private Connection connection;

    private DatabaseConfig() {
        String url = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : "jdbc:postgresql://localhost:5432/cu_mock";
        String user = System.getenv("DB_USERNAME") != null ? System.getenv("DB_USERNAME") : "postgres";
        String password = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : "postgres";

        int retries = 15;
        int delayMs = 3000;
        boolean connected = false;

        while (!connected && retries > 0) {
            try {
                logger.info("Attempting to connect to database: {} (retries left: {})", url, retries);
                this.connection = DriverManager.getConnection(url, user, password);
                logger.info("Database connection established");
                connected = true;
            } catch (SQLException e) {
                retries--;
                logger.warn("Failed to connect to database: {}. Retrying in {}ms ({} retries left)", e.getMessage(), delayMs, retries);
                if (retries == 0) {
                    logger.error("Could not connect to database after all retries", e);
                    throw new RuntimeException("Failed to connect to database", e);
                }
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during database connection retry", ie);
                }
            }
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