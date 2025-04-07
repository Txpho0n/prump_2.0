package org.example.config;

import com.fasterxml.jackson.databind.util.JSONPObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import org.json.JSONObject;


public class BotConfig {

    private static final String CONFIG_FILE = "config.json";

    public String getBotToken(){
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")){
            properties.load(fis);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties.getProperty("TOKEN");
    }

    public static String getTopic(){
        try {
            String content = new String(Files.readAllBytes(Paths.get(CONFIG_FILE)));
            JSONObject jsonObject = new JSONObject(content);
            return jsonObject.getString("currentTopic");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
