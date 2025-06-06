package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.BotConfig;
import org.example.config.DatabaseConfig;
import org.example.dao.InterviewDaoImpl;
import org.example.keyboards.KeyboardUtils;
import org.example.model.Interview;
import org.example.model.Rating;
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
    AWAITING_LEETCODE_USERNAME,
    AWAITING_INTERVIEW_SELECTION_FOR_CANCEL, 
    AWAITING_CANCELLATION_CONFIRMATION, 
    AWAITING_RATING,
    AWAITING_STATS_SELECTION,
    AWAITING_STATS_DATE_RANGE
}

public class Bot extends TelegramLongPollingBot {
    private final UserService userService;
    private final KeyboardUtils keyboardUtils;
    private final InterviewService interviewService;
    private final Map<String, BotState> userStates = new ConcurrentHashMap<>();
    private final Map<String, Interview> pendingInterviews = new ConcurrentHashMap<>();
    private final Map<String, String> pendingCancellationInterviewId = new ConcurrentHashMap<>();
    private final Manager manager;
    private final LeetCodeUtil leetCodeUtil;
    private final BotScheduler scheduler;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");



    private void showStatisticsMenu(String chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите тип статистики для просмотра:");
        
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        
        // User statistics
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(keyboardUtils.createButton("Пользователи (общая)", "stats_users_general"));
        keyboard.add(row1);
        
        // User activity statistics
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(keyboardUtils.createButton("Активность пользователей", "stats_users_activity"));
        keyboard.add(row2);
        
        // Interview statistics
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(keyboardUtils.createButton("Интервью (общая)", "stats_interviews_general"));
        keyboard.add(row3);
        
        // League distribution
        List<InlineKeyboardButton> row4 = new ArrayList<>();
        row4.add(keyboardUtils.createButton("Распределение по лигам", "stats_league_distribution"));
        keyboard.add(row4);
        
        // Ratings statistics
        List<InlineKeyboardButton> row5 = new ArrayList<>();
        row5.add(keyboardUtils.createButton("Статистика рейтингов", "stats_ratings"));
        keyboard.add(row5);
        
        // Recent users
        List<InlineKeyboardButton> row6 = new ArrayList<>();
        row6.add(keyboardUtils.createButton("Новые пользователи (7 дней)", "stats_recent_users"));
        keyboard.add(row6);
        
        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Error sending statistics menu: " + e.getMessage());
            sendMessage(chatId, "Ошибка при отображении меню статистики.", null);
            userStates.put(chatId, BotState.MAIN_MENU);
        }
    }

    private void sendMessage(String chatId, String text, InlineKeyboardMarkup markup) {
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
        if (state == BotState.AWAITING_CANCELLATION_CONFIRMATION) {
            handleCancellationConfirmation(chatId, messageText);
            return;
        }
        switch (messageText) {
            case "/start":
                if (!userService.userExists(chatId)) {
                    User newUser = createNewUser(update.getMessage());
                    userService.registerUser(newUser);
                    userService.setActive(chatId, true);
                    sendMessage(chatId, "Добро пожаловать! Укажите ваш LeetCode username.", null);
                    userStates.put(chatId, BotState.AWAITING_LEETCODE_USERNAME);
                } else {
                    userService.setActive(chatId, true);
                    showMainMenu(chatId, update.getMessage());
                    userStates.put(chatId, BotState.MAIN_MENU);
                }
                break;

            case "/help":
                String helpText = "Команды:\n/start - начать\n/help - помощь\n/interview - новое интервью\n" +
                                "/deactivate - отключить участие в интервью\n/activate - включить участие в интервью\n" +
                                "/cancel_interview - выберите интервью, которое хочется отменить\n" +
                                "/reset - сброс состояния (используйте, когда что-то зависло)\n" +
                                "/feedback - оставить отзыв о боте\n\n";
                                
                // Add admin commands if user is admin
                if (userService.isAdmin(chatId)) {
                    helpText += "Админ команды:\n/settopic - сменить тему\n/stats - просмотр статистики\n";
                }
                
                helpText += "Обязательно ознакомьтесь с инструкцией по подготовке к мок-интервью: " +
                            "https://teletype.in/@sidnevart_cu/SUcyzdPmr62\n" +
                            "И с инструкцией по тому что делать после создания интервью - " +
                            "https://teletype.in/@sidnevart_cu/i8PI0xFO_tt";
                            
                sendMessage(chatId, helpText, null);
                break;
            case "/feedback":
                sendMessage(chatId, "Пожалуйста, оставьте свой отзыв о боте. Мы ценим ваше мнение! Пройдите опрос по ссылке - https://docs.google.com/forms/d/e/1FAIpQLSeXZ5_UG_ZaIkktMT3E1QrYsFdfNXbeRvTR24grho2NdoOO1Q/viewform?usp=dialog", null);
                break;
            case "/interview":
                System.out.println("Пользователь " + chatId + " запросил интервью, текущее состояние: " + state);
                if (state == BotState.MAIN_MENU) {
                    if (userService.isActive(chatId)) {
                        startInterview(chatId);
                    } else {
                        sendMessage(chatId, "Вы деактивированы. Используйте /activate, чтобы участвовать в интервью.", null);
                    }
                } else {
                    sendMessage(chatId, "Сначала завершите регистрацию или вернитесь в главное меню с помощью /start.", null);
                }
                break;

            case "/deactivate":
                if (userService.userExists(chatId)) {
                    userService.setActive(chatId, false);
                    sendMessage(chatId, "Вы деактивированы. Теперь вы не будете участвовать в подборе для интервью. " +
                            "Используйте /activate, чтобы вернуться.",  null);
                    userStates.put(chatId, BotState.MAIN_MENU);
                } else {
                    sendMessage(chatId, "Сначала зарегистрируйтесь с помощью /start.", null);
                }
                break;

            case "/activate":
                if (userService.userExists(chatId)) {
                    userService.setActive(chatId, true);
                    sendMessage(chatId, "Вы активированы и снова можете участвовать в интервью!", null);
                    userStates.put(chatId, BotState.MAIN_MENU);
                } else {
                    sendMessage(chatId, "Сначала зарегистрируйтесь с помощью /start.", null);
                }
                break;

            case "/settopic":
                if (userService.isAdmin(chatId)) {
                    showTopicPicker(chatId);
                    userStates.put(chatId, BotState.AWAITING_ADMIN_TOPIC);
                } else {
                    sendMessage(chatId, "У вас нет прав администратора", null);
                }
                break;

            case "/reset":
                userStates.put(chatId, BotState.MAIN_MENU);
                sendMessage(chatId, "Состояние сброшено. Вы в главном меню.", null);
                break;

            case "/cancel_last_interview":
                if (state == BotState.MAIN_MENU) {
                    if (!userService.userExists(chatId)) {
                        sendMessage(chatId, "Сначала зарегистрируйтесь с помощью /start.", null);
                        return;
                    }
                    cancelNewInterview(chatId);
                } else {
                    sendMessage(chatId, "Вернитесь в главное меню с помощью /start.", null);
                }
                break;

            case "/cancel_interview":
                if (state == BotState.MAIN_MENU) {
                    if (!userService.userExists(chatId)) {
                        sendMessage(chatId, "Сначала зарегистрируйтесь с помощью /start.", null);
                        return;
                    }
                    showInterviewsForCancellation(chatId);
                } else {
                    sendMessage(chatId, "Вернитесь в главное меню с помощью /start.", null);
                }
                break;
            case "/stats":
                if (userService.isAdmin(chatId)) {
                    showStatisticsMenu(chatId);
                    userStates.put(chatId, BotState.AWAITING_STATS_SELECTION);
                } else {
                    sendMessage(chatId, "У вас нет прав администратора", null);
                }
                break;
                
            default:
                if (state == BotState.AWAITING_LEETCODE_USERNAME) {
                    String leetCodeUsername = messageText.trim();
                    try {
                        userService.updateLeetCodeUsername(chatId, leetCodeUsername);
                        manager.initialAssessment(leetCodeUsername);
                        User user = userService.getUserById(chatId);
                        String response = "✅ Рейтинг рассчитан!";
                        sendMessage(chatId, response, null);
                        showMainMenu(chatId, update.getMessage()); // Переход в MAIN_MENU
                        userStates.put(chatId, BotState.MAIN_MENU);
                    } catch (IOException e) {
                        sendMessage(chatId, "❌ Ошибка: Проверь правильность LeetCode username. Попробуй еще раз.", null);
                    } catch (Exception e) {
                        handleError(chatId, "Ошибка обработки. Попробуйте еще раз.", e);
                    }
                } else {
                    sendMessage(chatId, "Неизвестная команда. Используйте /help для списка команд.", null);
                }
                break;
        }
    }




    private void showInterviewsForCancellation(String chatId) {
        List<Interview> interviews = interviewService.findAllActiveInterviewsByTgId(chatId);
        if (interviews.isEmpty()) {
            sendMessage(chatId, "У вас нет запланированных интервью.", null);
            userStates.put(chatId, BotState.MAIN_MENU);
            return;
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText("Выберите интервью для отмены:");
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Interview interview : interviews) {
            String partnerId = interview.getPartner1Id().equals(chatId) ? interview.getPartner2Id() : interview.getPartner1Id();
            String partnerUsername = userService.getUserById(partnerId).getTgUsername();
            String task = interview.getPartner1Id().equals(chatId) ?
                    interview.getAssignedTaskForUser1() : interview.getAssignedTaskForUser2();
            String time = interview.getStart_time() != null ?
                    interview.getStart_time().format(FORMATTER) : "не указано";
            String buttonText = "@" + partnerUsername + ", задача: " + task + ", время: " + time;
            InlineKeyboardButton button = keyboardUtils.createButton(buttonText, "cancel_interview_" + interview.getId());
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(button);
            keyboard.add(row);
        }

        markup.setKeyboard(keyboard);
        message.setReplyMarkup(markup);
        try {
            execute(message);
            userStates.put(chatId, BotState.AWAITING_INTERVIEW_SELECTION_FOR_CANCEL);
        } catch (TelegramApiException e) {
            System.err.println("Failed to send interview selection: " + e.getMessage());
            sendMessage(chatId, "Ошибка при отображении интервью.", null);
            userStates.put(chatId, BotState.MAIN_MENU);
        }
    }

    private void handleCancellationConfirmation(String chatId, String messageText) {
        String interviewId = pendingCancellationInterviewId.get(chatId);
        if (interviewId == null) {
            sendMessage(chatId, "Ошибка: интервью не выбрано. Используйте /cancel_interview.", null);
            userStates.put(chatId, BotState.MAIN_MENU);
            pendingCancellationInterviewId.remove(chatId);
            return;
        }

        if (messageText.equalsIgnoreCase("да")) {
            try {
                Long id = Long.parseLong(interviewId);
                List<Interview> interviews = interviewService.findAllActiveInterviewsByTgId(chatId);
                Interview interview = interviews.stream()
                        .filter(i -> i.getId().equals(id))
                        .findFirst()
                        .orElse(null);
                if (interview == null) {
                    sendMessage(chatId, "Интервью не найдено или уже отменено.", null);
                } else {
                    String partnerId = interview.getPartner1Id().equals(chatId) ? interview.getPartner2Id() : interview.getPartner1Id();
                    String task = interview.getPartner1Id().equals(chatId) ?
                            interview.getAssignedTaskForUser1() : interview.getAssignedTaskForUser2();
                    String time = interview.getStart_time() != null ?
                            interview.getStart_time().format(FORMATTER) : "не указано";
                    String initiatorUsername = userService.getUserById(chatId).getTgUsername();
                    String partnerUsername = userService.getUserById(partnerId).getTgUsername();

                    interviewService.deleteInterview(id);

                    String message = "Интервью с @" + partnerUsername + " (время: " + time +
                            ") отменено пользователем @" + initiatorUsername;
                    sendMessage(chatId, message, null);
                    sendMessage(partnerId, message, null);
                    sendMessage(chatId, "Интервью успешно отменено.", null);
                }
            } catch (Exception e) {
                sendMessage(chatId, "Ошибка при отмене: " + e.getMessage(), null);
            }
        } else if (messageText.equalsIgnoreCase("нет")) {
            sendMessage(chatId, "Действие отменено.", null);
        } else {
            sendMessage(chatId, "Пожалуйста, напишите 'да' или 'нет'.", null);
            return;
        }

        pendingCancellationInterviewId.remove(chatId);
        userStates.put(chatId, BotState.MAIN_MENU);
    }


    private void cancelNewInterview(String chatId) {
        Interview pendingInterview = pendingInterviews.get(chatId);
        if (pendingInterview != null) {
            // Отмена недавно созданного интервью
            String partnerId = pendingInterview.getPartner2Id();
            interviewService.deleteInterview(pendingInterview.getId());
            pendingInterviews.remove(chatId);

            String task = pendingInterview.getAssignedTaskForUser1();
            String time = pendingInterview.getStart_time() != null ?
                    pendingInterview.getStart_time().format(FORMATTER) : "не указано";
            String initiatorUsername = userService.getUserById(chatId).getTgUsername();
            String partnerUsername = userService.getUserById(partnerId).getTgUsername();

            String message = "Интервью с @" + partnerUsername + " (время: " + time +
                    ") отменено пользователем @" + initiatorUsername;
            sendMessage(chatId, message, null);
            sendMessage(partnerId, message, null);
            userStates.put(chatId, BotState.MAIN_MENU);
        } else {
            // Поиск ближайшего интервью
            List<Interview> interviews = interviewService.findAllActiveInterviewsByTgId(chatId);
            if (interviews.isEmpty()) {
                sendMessage(chatId, "У вас нет запланированных интервью.", null);
                userStates.put(chatId, BotState.MAIN_MENU);
                return;
            }

            // Находим ближайшее интервью
            Interview nearest = null;
            for (Interview i : interviews) {
                if (i.getStart_time() != null) {
                    if (nearest == null || i.getStart_time().isBefore(nearest.getStart_time())) {
                        nearest = i;
                    }
                }
            }

            if (nearest == null) {
                sendMessage(chatId, "Нет интервью с указанным временем для отмены.", null);
                userStates.put(chatId, BotState.MAIN_MENU);
                return;
            }

            String partnerId = nearest.getPartner1Id().equals(chatId) ? nearest.getPartner2Id() : nearest.getPartner1Id();
            String task = nearest.getPartner1Id().equals(chatId) ?
                    nearest.getAssignedTaskForUser1() : nearest.getAssignedTaskForUser2();
            String time = nearest.getStart_time() != null ?
                    nearest.getStart_time().format(FORMATTER) : "не указано";
            String initiatorUsername = userService.getUserById(chatId).getTgUsername();
            String partnerUsername = userService.getUserById(partnerId).getTgUsername();

            interviewService.deleteInterview(nearest.getId());

            String message = "Интервью с @" + partnerUsername + " (задача: " + task + ", время: " + time +
                    ") отменено пользователем @" + initiatorUsername + "\\.";
            sendMessage(chatId, message, null);
            sendMessage(partnerId, message, null);
            userStates.put(chatId, BotState.MAIN_MENU);
        }
    }


    private void startInterview(String chatId) throws TelegramApiException, IOException, InterruptedException {
        User user1 = userService.getUserById(chatId);
        if (user1 == null) {
            sendMessage(chatId, "Ошибка: пользователь не найден.", null);
            return;
        }

        if(interviewService.findAllActiveInterviewsAmount(chatId) > 7){
            sendMessage(chatId, "Ошибка: у вас уже есть 7 запланированных интервью. Пожалуйста, завершите хотя бы одно из них перед созданием нового.", null);
            return;
        }

        String partnerId = manager.getPeerTelegramId(user1.getLeague());
        if (partnerId == null || partnerId.equals(chatId)) {
            sendMessage(chatId, "Ошибка: Попробуйте через минуту.", null);
            return;
        }

        User user2 = userService.getUserById(partnerId);
        if (user2 == null || !userService.isActive(partnerId)) {
            sendMessage(chatId, "Ошибка: Попробуйте через минуту.", null);
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
            sendMessage(chatId, "Не удалось подобрать задачи для интервью. Попробуйте позже!",null);
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
                null, chatId, partnerId, task1Slug, task2Slug, LocalDateTime.now().plusHours(2), LocalDateTime.now().plusHours(3)
        );

        Long generatedId = interviewService.scheduleInterview(interview);
        if (generatedId == null) {
            sendMessage(chatId, "Ошибка: не удалось создать интервью.",null);
            return;
        }
        interview.setId(generatedId);

        pendingInterviews.put(chatId, interview);

        // Инструкция для инициатора
        String initiatorMessage =
                "Ваш партнер: @" + user2.getTgUsername() + " (социальный рейтинг: " + user2.getSocialRating() + ")\n" +
                        "Задача которую вы будете проверять: " + task1Url + "\n" +
                        "📌 Свяжитесь с @" + user2.getTgUsername() + " в Telegram, чтобы обсудить детали.\n" +
                        "Выберите дату и время интервью через календарь ниже.\n\n" +
                        "Инструкция про то как подготовиться к интервью - https://teletype.in/@sidnevart_cu/SUcyzdPmr62 \n" +
                        "Инструкция про то что делать после создания интервью - https://teletype.in/@sidnevart_cu/i8PI0xFO_tt\n\n" +
                        "Если хотите отменить это интервью, используйте /cancel_last_interview";

        String partnerMessage =
                "Ваш партнер: @" + user1.getTgUsername() + " (социальный рейтинг: " + user1.getSocialRating() + ")\n" +
                        "Задача которую вы будете проверять: " + task2Url + "\n" +
                        "📌 Свяжитесь с @" + user1.getTgUsername() + " в Telegram, чтобы обсудить детали.\n" +
                        "@" + user1.getTgUsername() + " выберет дату\n\n" +
                        "Инструкция про то как подготовиться к интервью - https://teletype.in/@sidnevart_cu/SUcyzdPmr62 \n" +
                        "Инструкция про то что делать после создания интервью - https://teletype.in/@sidnevart_cu/i8PI0xFO_tt\n\n" +
                        "Если хотите отменить это интервью, используйте /cancel_last_interview";

        SendMessage message1 = new SendMessage();
        message1.setChatId(chatId);
        message1.setText(initiatorMessage);

        SendMessage message2 = new SendMessage();
        message2.setChatId(partnerId);
        message2.setText(partnerMessage);

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
                sendMessage(chatId, "Привет! Введи свой LeetCode username:",null);
                userStates.put(chatId, BotState.AWAITING_LEETCODE_USERNAME);
            } else {
                User user = userService.getUserById(chatId);
                String status = userService.isActive(chatId) ? "активен" : "деактивирован";
                String ratingText = user.getSocialRating() > 0 ? String.format("%.1f", user.getSocialRating()) : "нет оценок";
                sendMessage(chatId, buildWelcomeMessage(user) + "\nСоциальный рейтинг: " + ratingText + "\nСтатус: " + status, null);
                sendMessage(chatId, "Выберите действие:\n/interview - начать интервью\n/help - помощь\n/reset - сбросить состояние если бот завис\n/feedback - оставить отзыв о боте" +
                        "/deactivate - отключить участие\n/activate - включить участие",null);
                userStates.put(chatId, BotState.MAIN_MENU);
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

    private void showUsersGeneralStats(String chatId) {
        try {
            int totalUsers = userService.getTotalUsersCount();
            int activeUsers = userService.getActiveUsersCount();
            int usersWithLeetcode = userService.getUsersWithLeetcodeCount();
            
            StringBuilder sb = new StringBuilder();
            sb.append("📊 *Общая статистика пользователей*\n\n");
            sb.append("👤 Всего пользователей: ").append(totalUsers).append("\n");
            sb.append("✅ Активных пользователей: ").append(activeUsers).append(" (").append(String.format("%.1f", (activeUsers * 100.0 / totalUsers))).append("%)\n");
            sb.append("💻 Пользователей с LeetCode: ").append(usersWithLeetcode).append(" (").append(String.format("%.1f", (usersWithLeetcode * 100.0 / totalUsers))).append("%)\n");
            
            sendMessage(chatId, sb.toString(), null);
            
            // Возвращаемся в меню статистики после показа
            showStatisticsMenu(chatId);
        } catch (Exception e) {
            System.err.println("Error in showUsersGeneralStats: " + e.getMessage());
            e.printStackTrace();
            sendMessage(chatId, "Ошибка при получении статистики пользователей: " + e.getMessage(), null);
            userStates.put(chatId, BotState.MAIN_MENU);
        }
    }

    private void showUserActivityStats(String chatId) {
        try {
            int usersLast24h = userService.getUsersActiveInLastHours(24);
            int usersLast7d = userService.getUsersActiveInLastDays(7);
            int usersLast30d = userService.getUsersActiveInLastDays(30);
            
            StringBuilder sb = new StringBuilder();
            sb.append("📊 *Статистика активности пользователей*\n\n");
            sb.append("🕐 Активны за последние 24 часа: ").append(usersLast24h).append("\n");
            sb.append("📅 Активны за последние 7 дней: ").append(usersLast7d).append("\n");
            sb.append("📆 Активны за последние 30 дней: ").append(usersLast30d).append("\n");
            
            sendMessage(chatId, sb.toString(), null);
            
            showStatisticsMenu(chatId);
        } catch (Exception e) {
            System.err.println("Error in showUserActivityStats: " + e.getMessage());
            e.printStackTrace();
            sendMessage(chatId, "Ошибка при получении статистики активности: " + e.getMessage(), null);
            userStates.put(chatId, BotState.MAIN_MENU);
        }
    }

    private void showInterviewsGeneralStats(String chatId) {
        try {
            int totalInterviews = interviewService.getTotalInterviewsCount();
            int completedInterviews = interviewService.getCompletedInterviewsCount();
            int activeInterviews = interviewService.getActiveInterviewsCount();
            int interviews24h = interviewService.getInterviewsInLastHours(24);
            int interviews7d = interviewService.getInterviewsInLastDays(7);
            
            StringBuilder sb = new StringBuilder();
            sb.append("📊 *Статистика интервью*\n\n");
            sb.append("📝 Всего интервью: ").append(totalInterviews).append("\n");
            sb.append("✅ Завершенных интервью: ").append(completedInterviews);
            if (totalInterviews > 0) {
                sb.append(" (").append(String.format("%.1f", (completedInterviews * 100.0 / totalInterviews))).append("%)");
            }
            sb.append("\n");
            sb.append("⏳ Активных интервью: ").append(activeInterviews).append("\n");
            sb.append("🕐 Создано за последние 24 часа: ").append(interviews24h).append("\n");
            sb.append("📅 Создано за последние 7 дней: ").append(interviews7d).append("\n");
            
            sendMessage(chatId, sb.toString(), null);
            
            showStatisticsMenu(chatId);
        } catch (Exception e) {
            System.err.println("Error in showInterviewsGeneralStats: " + e.getMessage());
            e.printStackTrace();
            sendMessage(chatId, "Ошибка при получении статистики интервью: " + e.getMessage(), null);
            userStates.put(chatId, BotState.MAIN_MENU);
        }
    }

    private void showLeagueDistributionStats(String chatId) {
        try {
            Map<String, Integer> leagueDistribution = userService.getLeagueDistribution();
            int totalUsers = userService.getTotalUsersCount();
            
            StringBuilder sb = new StringBuilder();
            sb.append("📊 *Распределение пользователей по лигам*\n\n");
            
            for (Map.Entry<String, Integer> entry : leagueDistribution.entrySet()) {
                String league = entry.getKey();
                int count = entry.getValue();
                double percentage = (totalUsers > 0) ? (count * 100.0 / totalUsers) : 0;
                
                String leagueEmoji = "🏅";
                if (league.equalsIgnoreCase("Easy")) leagueEmoji = "🥉";
                else if (league.equalsIgnoreCase("Medium")) leagueEmoji = "🥈";
                else if (league.equalsIgnoreCase("Hard")) leagueEmoji = "🥇";
                
                sb.append(leagueEmoji).append(" ").append(league).append(": ")
                .append(count).append(" (").append(String.format("%.1f", percentage)).append("%)\n");
            }
            
            sendMessage(chatId, sb.toString(), null);
            
            showStatisticsMenu(chatId);
        } catch (Exception e) {
            System.err.println("Error in showLeagueDistributionStats: " + e.getMessage());
            e.printStackTrace();
            sendMessage(chatId, "Ошибка при получении статистики лиг: " + e.getMessage(), null);
            userStates.put(chatId, BotState.MAIN_MENU);
        }
    }

    private void showRatingsStats(String chatId) {
        try {
            double avgRating = userService.getAverageRating();
            int totalRatings = userService.getTotalRatingsCount();
            Map<Integer, Integer> ratingDistribution = userService.getRatingDistribution();
            
            StringBuilder sb = new StringBuilder();
            sb.append("📊 *Статистика рейтингов*\n\n");
            sb.append("⭐ Средний рейтинг: ").append(String.format("%.2f", avgRating)).append("/10\n");
            sb.append("📊 Всего оценок: ").append(totalRatings).append("\n\n");
            sb.append("*Распределение оценок:*\n");
            
            for (int i = 10; i >= 1; i--) {
                int count = ratingDistribution.getOrDefault(i, 0);
                double percentage = (totalRatings > 0) ? (count * 100.0 / totalRatings) : 0;
                sb.append(i).append("⭐: ").append(count)
                .append(" (").append(String.format("%.1f", percentage)).append("%)\n");
            }
            
            sendMessage(chatId, sb.toString(), null);
            
            showStatisticsMenu(chatId);
        } catch (Exception e) {
            System.err.println("Error in showRatingsStats: " + e.getMessage());
            e.printStackTrace();
            sendMessage(chatId, "Ошибка при получении статистики рейтингов: " + e.getMessage(), null);
            userStates.put(chatId, BotState.MAIN_MENU);
        }
    }

    private void showRecentUsers(String chatId) {
        try {
            List<User> recentUsers = userService.getRecentUsers(7);
            
            StringBuilder sb = new StringBuilder();
            sb.append("📊 *Новые пользователи за последние 7 дней*\n\n");
            
            if (recentUsers.isEmpty()) {
                sb.append("За последние 7 дней новых пользователей не было.");
            } else {
                for (User user : recentUsers) {
                    String username = user.getTgUsername() != null ? "@" + user.getTgUsername() : "Неизвестно";
                    String leetcode = user.getLeetcodeUsername() != null ? user.getLeetcodeUsername() : "Не указан";
                    String registrationDate = user.getRegistrationDate() != null ? 
                        user.getRegistrationDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "Неизвестно";
                    
                    sb.append("👤 ").append(username)
                    .append(" | LeetCode: ").append(leetcode)
                    .append(" | Дата: ").append(registrationDate)
                    .append(" | XP: ").append(user.getXp())
                    .append("\n");
                }
            }
            
            sendMessage(chatId, sb.toString(), null);
            
            showStatisticsMenu(chatId);
        } catch (Exception e) {
            System.err.println("Error in showRecentUsers: " + e.getMessage());
            e.printStackTrace();
            sendMessage(chatId, "Ошибка при получении списка новых пользователей: " + e.getMessage(), null);
            userStates.put(chatId, BotState.MAIN_MENU);
        }
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
                    sendMessage(telegramId, "Нельзя выбрать дату в прошлом. Пожалуйста, выберите сегодняшнюю или будущую дату.",null);
                    showDatePicker(telegramId, today);
                    return;
                }
                showTimePicker(chatId, selectedDate);
                userStates.put(telegramId, BotState.AWAITING_INTERVIEW_TIME);
            } else if (state == BotState.AWAITING_INTERVIEW_TIME && callbackData.startsWith("time_")) {
                String[] parts = callbackData.split("_");
                LocalDateTime dateTime = LocalDate.parse(parts[1]).atTime(Integer.parseInt(parts[2].split(":")[0]), 0);
                if (dateTime.isBefore(LocalDateTime.now())) {
                    sendMessage(telegramId, "Нельзя выбрать время в прошлом. Пожалуйста, выберите другое время.",null);
                    showTimePicker(chatId, LocalDate.parse(parts[1]));
                    return;
                }

                Interview interview = pendingInterviews.get(telegramId);
                if (interview == null) {
                    sendMessage(telegramId, "Ошибка: интервью не найдено. Начните заново с /interview",null);
                    userStates.put(telegramId, BotState.MAIN_MENU);
                    return;
                }

                interview.setStart_time(dateTime);
                interview.setEnd_time(dateTime.plusHours(1));
                interviewService.updateInterview(interview);

                sendMessage(telegramId, "Интервью запланировано на " + dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),null);
                sendMessage(interview.getPartner2Id(), "Интервью с @" + userService.getUserById(telegramId).getTgUsername() +
                        " запланировано на " + dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")),null);

                pendingInterviews.remove(telegramId);
                userStates.put(telegramId, BotState.MAIN_MENU);
            } else if (state == BotState.AWAITING_ADMIN_TOPIC && callbackData.startsWith("topic_")) {
                String newTopic = callbackData.split("_")[1];
                BotConfig.setTopic(newTopic);
                sendMessage(telegramId, "Тема изменена на: " + newTopic,null);
                userStates.put(telegramId, BotState.MAIN_MENU);
            } else if (state == BotState.AWAITING_INTERVIEW_SELECTION_FOR_CANCEL && callbackData.startsWith("cancel_interview_")) {
                String interviewId = callbackData.replace("cancel_interview_", "");
                pendingCancellationInterviewId.put(telegramId, interviewId);
                sendMessage(telegramId, "Вы уверены, что хотите отменить это интервью? Напишите 'да' для подтверждения или 'нет' для отмены.",null);
                userStates.put(telegramId, BotState.AWAITING_CANCELLATION_CONFIRMATION);
            } else if (callbackData.startsWith("rate_")) {
                handleRatingCallback(telegramId, callbackData);
            } else if (state == BotState.AWAITING_STATS_SELECTION && callbackData.startsWith("stats_")) {
                String statType = callbackData.substring(6); // Remove "stats_" prefix
                try {
                    switch (statType) {
                        case "users_general":
                            showUsersGeneralStats(telegramId);
                            break;
                        case "users_activity":
                            showUserActivityStats(telegramId);
                            break;
                        case "interviews_general":
                            showInterviewsGeneralStats(telegramId);
                            break;
                        case "league_distribution":
                            showLeagueDistributionStats(telegramId);
                            break;
                        case "ratings":
                            showRatingsStats(telegramId);
                            break;
                        case "recent_users":
                            showRecentUsers(telegramId);
                            break;
                        default:
                            sendMessage(telegramId, "Неизвестный тип статистики", null);
                            showStatisticsMenu(telegramId);
                            break;
                    }
                } catch (Exception e) {
                    System.err.println("Error showing statistics: " + e.getMessage());
                    e.printStackTrace();
                    sendMessage(telegramId, "Ошибка при получении статистики: " + e.getMessage(), null);
                }
            }  
             else {
                sendMessage(telegramId, "Ошибка: неверное действие. Используйте /start.",null);
                userStates.put(telegramId, BotState.MAIN_MENU);
            }
        } catch (Exception e) {
            System.err.println("Error in handleCallback: " + e.getMessage());
            sendMessage(telegramId, "Ошибка при обработке выбора. Попробуйте снова.",null);
            userStates.put(telegramId, BotState.MAIN_MENU);
        }
    }
    private void handleRatingCallback(String telegramId, String callbackData) {
        try {
            String[] parts = callbackData.split("_");
            Long interviewId = Long.parseLong(parts[1]);
            String ratedId = parts[2];
            Integer rating = Integer.parseInt(parts[3]);

            Rating userRating = new Rating(telegramId, ratedId, interviewId, rating);
            userService.saveRating(userRating);
            userService.updateSocialRating(ratedId);

            sendMessage(telegramId, "Спасибо за оценку!",null);
            userStates.put(telegramId, BotState.MAIN_MENU);
        } catch (Exception e) {
            sendMessage(telegramId, "Ошибка при сохранении оценки: " + e.getMessage(),null);
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
                true,
                0.0
        );
    }

    private String buildWelcomeMessage(User user) {
        return "Привет, " + user.getFullName() + "!\nТвой рейтинг: " + user.getXp();
    }



    private void handleError(String chatId, String context, Exception e) {
        System.err.println(context + ": " + e.getMessage());
        sendMessage(chatId, "⚠ Ошибка: " + context,null);
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
