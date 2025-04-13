package org.example.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BotConfig {
    private static final DatabaseConfig dbConfig;
    private final ObjectMapper mapper;

    public BotConfig() {
        this.dbConfig = DatabaseConfig.getInstance();
        this.mapper = new ObjectMapper();
        // Инициализация topic из config.json, если запись в БД отсутствует
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT value FROM config WHERE key = ?")) {
            stmt.setString(1, "topic");
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                // Если topic нет в БД, читаем из config.json
                String initialTopic = "Array";
                setTopic(initialTopic);
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации config", e);
        }
    }


    public static String getTopic() {
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT value FROM config WHERE key = ?")) {
            stmt.setString(1, "topic");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("value");
            }
            throw new RuntimeException("Topic не найден в БД");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка чтения темы", e);
        }
    }

    public static void setTopic(String newTopic) {
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO config (key, value) VALUES (?, ?) " +
                     "ON CONFLICT (key) DO UPDATE SET value = EXCLUDED.value")) {
            stmt.setString(1, "topic");
            stmt.setString(2, newTopic);
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка записи темы", e);
        }
    }
}
