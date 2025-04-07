package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import org.example.config.DatabaseConfig;
import org.example.model.User;
import org.example.service.UserService;
import org.example.util.LeetCodeUtil;
import org.example.util.Manager;
import org.example.config.BotConfig;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDateTime;

public class Bot extends TelegramLongPollingBot {
    private final UserService userService;
    private final Manager manager;

    public Bot() throws Exception {
        Connection connection = DatabaseConfig.getInstance().getConnection();
        this.userService = new UserService(connection);
        this.manager = new Manager(connection);
    }

    @Override
    public String getBotUsername() {
        return "cu_algo_bot";
    }

    @Override
    public String getBotToken() {
        return "7743800574:AAEcNPYS9TQGwRyChKs4ck0a6kJujksJUQw";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String chatId = message.getChatId().toString();
            String telegramId = message.getFrom().getId().toString();
            String messageText = message.getText();

            if (messageText.startsWith("/")) {
                handleCommands(chatId, telegramId, messageText, message);
            } else {
                handleLeetCodeUsernameInput(chatId, telegramId, messageText);
            }
        }
    }

    private void handleCommands(String chatId, String telegramId, String command, Message message) {
        switch (command) {
            case "/start":
                handleStartCommand(chatId, telegramId, message);
                break;
            case "/help":
                sendHelpMessage(chatId);
                break;
            case "/interview":
                scheduleInterview(chatId);
                break;
        }
    }

    private void handleStartCommand(String chatId, String telegramId, Message message) {
        try {
            if (!userService.userExists(telegramId)) {
                User newUser = createNewUser(message);
                userService.registerUser(newUser);
                sendMessage(chatId, "Привет! Введи свой LeetCode username:");
            } else {
                User user = userService.getUserById(telegramId);
                sendMessage(chatId, buildWelcomeMessage(user));
            }
        } catch (Exception e) {
            handleError(chatId, "Ошибка при старте", e);
        }
    }

    private void handleLeetCodeUsernameInput(String chatId, String telegramId, String leetcodeUsername) {
        try {
            // Обновляем leetcode_username для пользователя
            userService.updateLeetCodeUsername(telegramId, leetcodeUsername);

            // Рассчитываем XP
            manager.initialAssessment(leetcodeUsername);

            // Получаем обновленные данные
            User user = userService.getUserById(telegramId);

            // Формируем ответ
            String response = "✅ Рейтинг рассчитан!\nТвой XP: " + user.getXp();
            sendMessage(chatId, response);

        } catch (IOException e) {
            sendMessage(chatId, "❌ Ошибка: Проверь правильность LeetCode username");
        } catch (Exception e) {
            handleError(chatId, "Ошибка обработки", e);
        }
    }

    private User createNewUser(Message message) {
        String firstName = message.getFrom().getFirstName();
        String lastName = message.getFrom().getLastName();
        String fullName = (lastName != null) ? firstName + " " + lastName : firstName;

        return new User(
                message.getFrom().getUserName(),
                null, // leetcode_username будет установлен позже
                "Новичок",
                message.getFrom().getId().toString(),
                0L,
                fullName,
                null,
                null,
                LocalDateTime.now(),
                false
        );
    }

    private String buildWelcomeMessage(User user) {
        return "С возвращением, " + user.getFullName() + "!\nТвой рейтинг: " + user.getXp();
    }

    private void sendHelpMessage(String chatId) {
        String currentTopic = BotConfig.getTopic();
        String helpText = """
        Доступные команды:
        /start - Начать работу
        /help - Помощь
        /interview - Собеседование
        
        Текущая тема: %s
        """.formatted(currentTopic);

        sendMessage(chatId, helpText);
    }

    private void scheduleInterview(String chatId) {
        sendMessage(chatId, "⏳ Функционал в разработке");
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка отправки: " + e.getMessage());
        }
    }

    private void handleError(String chatId, String context, Exception e) {
        System.err.println(context + ": " + e.getMessage());
        sendMessage(chatId, "⚠ Ошибка: " + context);
    }
}