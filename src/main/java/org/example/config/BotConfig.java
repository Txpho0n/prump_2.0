package org.example.config;

import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class BotConfig {
    private static final String CONFIG_JSON = "config.json";
    private static final String CONFIG_PROPERTIES = "config.properties";

    // Для токена
    public String getBotToken() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(CONFIG_PROPERTIES)) {
            if (inputStream == null) {
                throw new RuntimeException("Файл " + CONFIG_PROPERTIES + " не найден в ресурсах");
            }
            properties.load(inputStream);
            return properties.getProperty("TOKEN");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка чтения конфига", e);
        }
    }

    // Для темы
    public static String getTopic() {
        try (InputStream inputStream = BotConfig.class.getClassLoader().getResourceAsStream(CONFIG_JSON)) {
            if (inputStream == null) {
                throw new RuntimeException("Файл " + CONFIG_JSON + " не найден в ресурсах");
            }
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(content);
            return jsonObject.getString("currentTopic");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка чтения темы", e);
        }
    }
}