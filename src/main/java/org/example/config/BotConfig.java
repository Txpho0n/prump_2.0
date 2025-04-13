package org.example.config;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class BotConfig {
    private static final String CONFIG_JSON = "src/main/resources/config.json";
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
        try {
            String content = Files.readString(Paths.get(CONFIG_JSON), StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(content);
            System.out.println(jsonObject.getString("currentTopic"));
            return jsonObject.getString("currentTopic");
        } catch (Exception e) {
            throw new RuntimeException("Ошибка чтения темы", e);
        }
    }

    public static void setTopic(String newTopic) {
        try {
            String content = Files.readString(Paths.get(CONFIG_JSON), StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(content);
            jsonObject.put("currentTopic", newTopic);

            Files.writeString(Paths.get(CONFIG_JSON), jsonObject.toString(4), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка записи темы", e);
        }
    }
}
