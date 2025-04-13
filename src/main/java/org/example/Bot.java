package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.BotConfig;
import org.example.config.DatabaseConfig;
import org.example.keyboards.KeyboardUtils;
import org.example.model.Interview;
import org.example.model.User;
import org.example.multithreading.BotScheduler;
import org.example.service.InterviewService;
import org.example.service.UserService;
import org.example.util.LeetCodeUtil;
import org.example.util.Manager;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

enum BotState {
    START,
    MAIN_MENU,
    AWAITING_INTERVIEW_DATE,
    AWAITING_INTERVIEW_TIME,
    AWAITING_ADMIN_TOPIC,
    AWAITING_LEETCODE_USERNAME
}

public class Bot extends TelegramLongPollingBot {
    private final UserService userService;
    private final KeyboardUtils keyboardUtils;
    private final InterviewService interviewService;
    private final Map<String, BotState> userStates = new ConcurrentHashMap<>();
    private final Map<String, Interview> pendingInterviews = new ConcurrentHashMap<>();
    private final Manager manager;
    private final LeetCodeUtil leetCodeUtil;
    private final BotScheduler scheduler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final List<String> availableTopics = Arrays.asList(
            "Array", "String", "Hash Table", "Dynamic Programming", "Math", "Sorting", "Greedy",
            "Depth-First Search", "Binary Search", "Database", "Matrix", "Breadth-First Search",
            "Tree", "Bit Manipulation", "Two Pointers", "Prefix Sum", "Heap (Priority Queue)",
            "Simulation", "Binary Tree", "Stack", "Graph", "Counting", "Sliding Window", "Design",
            "Enumeration", "Backtracking", "Union Find", "Linked List", "Ordered Set", "Number Theory",
            "Monotonic Stack", "Segment Tree", "Trie", "Combinatorics", "Bitmask", "Queue", "Recursion",
            "Divide and Conquer", "Memoization", "Binary Indexed Tree", "Geometry", "Binary Search Tree",
            "Hash Function", "String Matching", "Topological Sort", "Shortest Path", "Rolling Hash",
            "Game Theory", "Interactive", "Data Stream", "Monotonic Queue", "Brainteaser", "Doubly-Linked List",
            "Randomized", "Merge Sort", "Counting Sort", "Iterator", "Concurrency", "Probability and Statistics",
            "Quickselect", "Suffix Array", "Bucket Sort", "Line Sweep", "Minimum Spanning Tree", "Shell",
            "Reservoir Sampling", "Strongly Connected Component", "Eulerian Circuit", "Radix Sort",
            "Rejection Sampling", "Biconnected Component"
    );

    public Bot() throws Exception {
        Connection connection = DatabaseConfig.getInstance().getConnection();
        this.interviewService = new InterviewService();
        this.userService = new UserService();
        this.manager = new Manager();
        this.keyboardUtils = new KeyboardUtils();
        this.leetCodeUtil = new LeetCodeUtil();
        this.scheduler = new BotScheduler(interviewService, userService, leetCodeUtil, manager, this::sendMessage);
        loadCurrentTopic();
    }

    @Override
    public String getBotUsername() {
        return "cu_algo_bot";
    }
    // 8193864295:AAHG-uCB89lL4iiUGN3t0O3zogLGE2gIFPk - prod
    // 7743800574:AAEcNPYS9TQGwRyChKs4ck0a6kJujksJUQw -local for tests
    // плохая практика лучше так не делать а забить в .env
    @Override
    public String getBotToken() {
        return "8193864295:AAHG-uCB89lL4iiUGN3t0O3zogLGE2gIFPk";
    }

    @Override
    public void onUpdateReceived(Update update) {
        long chatId = update.hasMessage() ? update.getMessage().getChatId() : update.getCallbackQuery().getMessage().getChatId();
        String telegramId = String.valueOf(chatId);
        userStates.putIfAbsent(telegramId, BotState.START);
        System.out.println("User " + telegramId + " state: " + userStates.get(telegramId)); // Лог состояния

        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleMessage(update);
            } else if (update.hasCallbackQuery()) {
                handleCallback(update);
            }
        } catch (Exception e) {
            System.err.println("Error in onUpdateReceived: " + e.getMessage());
        }
    }

    private void handleMessage(Update update) throws TelegramApiException, IOException, InterruptedException {
        String messageText = update.getMessage().getText();
        String chatId = String.valueOf(update.getMessage().getChatId());
        BotState state = userStates.get(chatId);
        System.out.println("Handling message: " + messageText + ", state: " + state); // Лог команды и состояния

        switch (messageText) {
            case "/start":
                if (!userService.userExists(chatId)) {
                    User newUser = createNewUser(update.getMessage());
                    userService.registerUser(newUser);
                    userService.setActive(chatId, true);
                    sendMessage(chatId, "Добро пожаловать! Укажите ваш LeetCode username.");
                    userStates.put(chatId, BotState.AWAITING_LEETCODE_USERNAME);
                } else {
                    userService.setActive(chatId, true);
                    showMainMenu(chatId, update.getMessage());
                    userStates.put(chatId, BotState.MAIN_MENU);
                }
                break;

            case "/help":
                sendMessage(chatId, "Команды:\n/start - начать\n/help - помощь\n/interview - новое интервью\n" +
                        "/deactivate - отключить участие в интервью\n/activate - включить участие в интервью\n" +
                        "/settopic - сменить тему (админ)\n\nОбязательно ознакомьтесь с инструкцией по подготовке к мок-интервью: https://teletype.in/@sidnevart_cu/SUcyzdPmr62\nИ с инструкцией по тому что делать после создания интервью - https://teletype.in/@sidnevart_cu/i8PI0xFO_tt");
                break;

            case "/interview":
                System.out.println("Пользователь " + chatId + " запросил интервью, текущее состояние: " + state);
                if (state == BotState.MAIN_MENU) {
                    if (userService.isActive(chatId)) {
                        startInterview(chatId);
                    } else {
                        sendMessage(chatId, "Вы деактивированы. Используйте /activate, чтобы участвовать в интервью.");
                    }
                } else {
                    sendMessage(chatId, "Сначала завершите регистрацию или вернитесь в главное меню с помощью /start.");
                }
                break;

            case "/deactivate":
                if (userService.userExists(chatId)) {
                    userService.setActive(chatId, false);
                    sendMessage(chatId, "Вы деактивированы. Теперь вы не будете участвовать в подборе для интервью. " +
                            "Используйте /activate, чтобы вернуться.");
                    userStates.put(chatId, BotState.MAIN_MENU);
                } else {
                    sendMessage(chatId, "Сначала зарегистрируйтесь с помощью /start.");
                }
                break;

            case "/activate":
                if (userService.userExists(chatId)) {
                    userService.setActive(chatId, true);
                    sendMessage(chatId, "Вы активированы и снова можете участвовать в интервью!");
                    userStates.put(chatId, BotState.MAIN_MENU);
                } else {
                    sendMessage(chatId, "Сначала зарегистрируйтесь с помощью /start.");
                }
                break;

            case "/settopic":
                if (userService.isAdmin(chatId)) {
                    showTopicPicker(chatId);
                    userStates.put(chatId, BotState.AWAITING_ADMIN_TOPIC);
                } else {
                    sendMessage(chatId, "У вас нет прав администратора");
                }
                break;

            case "/reset":
                userStates.put(chatId, BotState.MAIN_MENU);
                sendMessage(chatId, "Состояние сброшено. Вы в главном меню.");
                break;

            default:
                if (state == BotState.AWAITING_LEETCODE_USERNAME) {
                    String leetCodeUsername = messageText.trim();
                    try {
                        userService.updateLeetCodeUsername(chatId, leetCodeUsername);
                        manager.initialAssessment(leetCodeUsername);
                        User user = userService.getUserById(chatId);
                        String response = "✅ Рейтинг рассчитан!\nТвой XP: " + user.getXp();
                        sendMessage(chatId, response);
                        showMainMenu(chatId, update.getMessage()); // Переход в MAIN_MENU
                        userStates.put(chatId, BotState.MAIN_MENU);
                    } catch (IOException e) {
                        sendMessage(chatId, "❌ Ошибка: Проверь правильность LeetCode username");
                    } catch (Exception e) {
                        handleError(chatId, "Ошибка обработки", e);
                    }
                } else {
                    sendMessage(chatId, "Неизвестная команда. Используйте /help для списка команд.");
                }
                break;
        }
    }



    private void startInterview(String chatId) throws TelegramApiException, IOException, InterruptedException {
        User user1 = userService.getUserById(chatId);
        if (user1 == null) {
            sendMessage(chatId, "Ошибка: пользователь не найден.");
            return;
        }

        String partnerId = manager.getPeerTelegramId(user1.getLeague());
        if (partnerId == null || partnerId.equals(chatId)) {
            sendMessage(chatId, "Ошибка: Попробуйте через минуту.");
            return;
        }

        User user2 = userService.getUserById(partnerId);
        if (user2 == null || !userService.isActive(partnerId)) {
            sendMessage(chatId, "Ошибка: Попробуйте через минуту.");
            return;
        }

        String currentTopic = BotConfig.getTopic();
        JsonNode task1 = null;
        JsonNode task2 = null;
        try {
            task1 = manager.matchTask(user1.getXp());
            task2 = manager.matchTask(user2.getXp());
        } catch (IOException e) {
            System.err.println("Failed to match tasks: " + e.getMessage());
            sendMessage(chatId, "Не удалось подобрать задачи для интервью. Попробуйте позже!");
            return;
        }

        String task1Slug = task1.get("titleSlug").asText();
        String task2Slug = task2.get("titleSlug").asText();
        String task1Difficulty = task1.get("difficulty").asText();
        String task2Difficulty = task2.get("difficulty").asText();
        String task1Title = task1.has("title") ? task1.get("title").asText() : task1Slug;
        String task2Title = task2.has("title") ? task2.get("title").asText() : task2Slug;
        System.out.println(task1Slug + " " + task1Difficulty + " " + task1Title);
        String task1Url = "https://leetcode.com/problems/" + task1Slug + "/";
        String task2Url = "https://leetcode.com/problems/" + task2Slug + "/";

        Interview interview = new Interview(
                null, chatId, partnerId, task1Slug, task2Slug, LocalDateTime.now().plusDays(7), LocalDateTime.now().plusDays(7).plusHours(1)
        );

        Long generatedId = interviewService.scheduleInterview(interview);
        if (generatedId == null) {
            sendMessage(chatId, "Ошибка: не удалось создать интервью.");
            return;
        }
        interview.setId(generatedId);

        pendingInterviews.put(chatId, interview);

        // Инструкция для инициатора
        String initiatorMessage =
                "Ваш партнер: @" + user2.getTgUsername() + "\n" +
                        "Задача которую вы будете проверять: " + task1Url + "\n" +
                        "📌 Свяжитесь с " + user2.getTgUsername() + " в Telegram, чтобы обсудить детали.\n" +
                        "Выберите дату и время интервью через календарь ниже.\n\n"+
                        "Инструкция про то как подготовиться к интервью - https://teletype.in/@sidnevart_cu/SUcyzdPmr62 \n" +
                        "Инструкция про то что делать после создания интервью - https://teletype.in/@sidnevart_cu/i8PI0xFO_tt";


        // Инструкция для партнёра
        String partnerMessage =
                "Ваш партнер: @" + user1.getTgUsername() + "\n" +
                        "Задача которую вы будете проверять: " + task2Url + "\n" +
                        "📌 Свяжитесь с " + user1.getTgUsername() + " в Telegram, чтобы обсудить детали.\n" +
                        "@"+user1.getTgUsername()+" выберет дату"+"\n\n"+
                        "Инструкция про то как подготовиться к интервью - https://teletype.in/@sidnevart_cu/SUcyzdPmr62 \n" +
                        "Инструкция про то что делать после создания интервью - https://teletype.in/@sidnevart_cu/i8PI0xFO_tt";

        SendMessage message1 = new SendMessage();
        message1.setChatId(chatId);
        message1.setText(initiatorMessage);
        message1.enableMarkdown(true);

        SendMessage message2 = new SendMessage();
        message2.setParseMode("MarkdownV2");
        message2.setChatId(partnerId);
        message2.setText(partnerMessage);
        message2.enableMarkdown(true);

        try {
            executeAsync(message1, new SentCallback<Message>() {
                @Override
                public void onResult(BotApiMethod<Message> botApiMethod, Message message) {
                    System.out.println("Message sent to " + chatId + ": " + initiatorMessage);
                }

                @Override
                public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
                    System.err.println("Telegram API error sending message to " + chatId + ": " + e.getApiResponse());
                }

                @Override
                public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
                    System.err.println("Exception sending message to " + chatId + ": " + e.getMessage());
                }
            });

            executeAsync(message2, new SentCallback<Message>() {
                @Override
                public void onResult(BotApiMethod<Message> botApiMethod, Message message) {
                    System.out.println("Message sent to " + partnerId + ": " + partnerMessage);
                }

                @Override
                public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
                    System.err.println("Telegram API error sending message to " + partnerId + ": " + e.getApiResponse());
                }

                @Override
                public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
                    System.err.println("Exception sending message to " + partnerId + ": " + e.getMessage());
                }
            });
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TelegramApiException e) {
            System.err.println("Failed to initiate message sending: " + e.getMessage());
        }

        showDatePicker(chatId, LocalDate.now());
        userStates.put(chatId, BotState.AWAITING_INTERVIEW_DATE);
    }

    private void showMainMenu(String chatId, Message message) throws TelegramApiException {
        try {
            if (!userService.userExists(chatId)) {
                User newUser = createNewUser(message);
                userService.registerUser(newUser);
                sendMessage(chatId, "Привет! Введи свой LeetCode username:");
                userStates.put(chatId, BotState.AWAITING_LEETCODE_USERNAME);
            } else {
                User user = userService.getUserById(chatId);
                String status = userService.isActive(chatId) ? "активен" : "деактивирован";
                sendMessage(chatId, buildWelcomeMessage(user) + "\nСтатус: " + status);
                sendMessage(chatId, "Добро пожаловать! Выберите действие:\n/interview - начать интервью\n/help - помощь\n" +
                        "/deactivate - отключить участие\n/activate - включить участие");
                userStates.put(chatId, BotState.MAIN_MENU); // Явно устанавливаем MAIN_MENU
            }
        } catch (Exception e) {
            handleError(chatId, "Ошибка при старте", e);
        }
    }

    private void showDatePicker(String chatId, LocalDate startDate) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите дату интервью (не ранее сегодняшнего дня):");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = keyboardUtils.buildCalendar(startDate);
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);
        execute(message);
    }

    private void showTimePicker(long chatId, LocalDate selectedDate) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Выберите время для " + selectedDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + ":");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        String[] times = {"10:00", "12:00", "14:00", "16:00", "18:00", "20:00"};
        for (int i = 0; i < times.length; i += 2) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(keyboardUtils.createButton(times[i], "time_" + selectedDate.toString() + "_" + times[i]));
            if (i + 1 < times.length) {
                row.add(keyboardUtils.createButton(times[i + 1], "time_" + selectedDate.toString() + "_" + times[i + 1]));
            }
            keyboard.add(row);
        }
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);
        execute(message);
    }

    private void showTopicPicker(String chatId) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите новую тему (текущая: " + BotConfig.getTopic() + "):");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        for (String topic : availableTopics) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(keyboardUtils.createButton(topic, "topic_" + topic));
            keyboard.add(row);
        }
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);
        execute(message);
    }

    private void handleCallback(Update update) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String telegramId = String.valueOf(chatId);
        BotState state = userStates.get(telegramId);

        try {
            if (state == BotState.AWAITING_INTERVIEW_DATE && callbackData.startsWith("date_")) {
                String dateStr = callbackData.split("_")[1];
                LocalDate selectedDate = LocalDate.parse(dateStr);
                LocalDate today = LocalDate.now();
                if (selectedDate.isBefore(today)) {
                    sendMessage(telegramId, "Нельзя выбрать дату в прошлом. Пожалуйста, выберите сегодняшнюю или будущую дату.");
                    showDatePicker(telegramId, today);
                    return;
                }
                showTimePicker(chatId, selectedDate);
                userStates.put(telegramId, BotState.AWAITING_INTERVIEW_TIME);
            } else if (state == BotState.AWAITING_INTERVIEW_TIME && callbackData.startsWith("time_")) {
                String[] parts = callbackData.split("_");
                LocalDateTime dateTime = LocalDate.parse(parts[1]).atTime(Integer.parseInt(parts[2].split(":")[0]), 0);
                if (dateTime.isBefore(LocalDateTime.now())) {
                    sendMessage(telegramId, "Нельзя выбрать время в прошлом. Пожалуйста, выберите другое время.");
                    showTimePicker(chatId, LocalDate.parse(parts[1]));
                    return;
                }

                Interview interview = pendingInterviews.get(telegramId);
                if (interview == null) {
                    sendMessage(telegramId, "Ошибка: интервью не найдено. Начните заново с /interview");
                    userStates.put(telegramId, BotState.MAIN_MENU);
                    return;
                }

                interview.setStart_time(dateTime);
                interview.setEnd_time(dateTime.plusHours(1));
                interviewService.updateInterview(interview);

                sendMessage(telegramId, "Интервью запланировано на " + dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
                sendMessage(interview.getPartner2Id(), "Интервью с @" + userService.getUserById(telegramId).getTgUsername() +
                        " запланировано на " + dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));

                pendingInterviews.remove(telegramId);
                userStates.put(telegramId, BotState.MAIN_MENU);
            } else if (state == BotState.AWAITING_ADMIN_TOPIC && callbackData.startsWith("topic_")) {
                String newTopic = callbackData.split("_")[1];
                BotConfig.setTopic(newTopic);
                sendMessage(telegramId, "Тема изменена на: " + newTopic);
                userStates.put(telegramId, BotState.MAIN_MENU);
            }
        } catch (Exception e) {
            System.err.println("Error in handleCallback: " + e.getMessage());
            sendMessage(telegramId, "Ошибка при обработке выбора. Попробуйте снова.");
            userStates.put(telegramId, BotState.MAIN_MENU);
        }
    }

    private User createNewUser(Message message) {
        String firstName = message.getFrom().getFirstName();
        String lastName = message.getFrom().getLastName();
        String fullName = (lastName != null) ? firstName + " " + lastName : firstName;

        return new User(
                message.getFrom().getUserName(),
                String.valueOf(message.getFrom().getId()),
                null,
                0L,
                "Easy",
                fullName,
                null,
                null,
                null,
                false,
                true
        );
    }

    private String buildWelcomeMessage(User user) {
        return "С возвращением, " + user.getFullName() + "!\nТвой рейтинг: " + user.getXp();
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            executeAsync(message, new SentCallback<Message>() {
                @Override
                public void onResult(BotApiMethod<Message> botApiMethod, Message message) {
                    System.out.println("Message sent to " + chatId + ": " + text);
                }

                @Override
                public void onError(BotApiMethod<Message> botApiMethod, TelegramApiRequestException e) {
                    System.err.println("Telegram API error sending message to " + chatId + ": " + e.getApiResponse());
                }

                @Override
                public void onException(BotApiMethod<Message> botApiMethod, Exception e) {
                    System.err.println("Exception sending message to " + chatId + ": " + e.getMessage());
                }
            });
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TelegramApiException e) {
            System.err.println("Failed to initiate message sending to " + chatId + ": " + e.getMessage());
        }
    }

    private void handleError(String chatId, String context, Exception e) {
        System.err.println(context + ": " + e.getMessage());
        sendMessage(chatId, "⚠ Ошибка: " + context);
    }

    @Override
    public void onClosing() {
        scheduler.shutdown();
    }

    private void loadCurrentTopic() {
        String topic = BotConfig.getTopic();
        if (!availableTopics.contains(topic)) {
            BotConfig.setTopic("Arrays");
        }
    }
}