package org.example.multithreading;


import com.fasterxml.jackson.databind.JsonNode;
import org.example.model.Interview;
import org.example.model.User;
import org.example.service.InterviewService;
import org.example.service.UserService;
import org.example.util.LeetCodeUtil;
import org.example.util.Manager;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BotScheduler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final InterviewService interviewService;
    private final UserService userService;
    private final LeetCodeUtil leetCodeUtil;
    private final Manager manager;
    private final SendMessageCallback sendMessageCallback;

    // Интерфейс для отправки сообщений из бота
    public interface SendMessageCallback {
        void sendMessage(String chatId, String text, InlineKeyboardMarkup markup) throws TelegramApiException;
    }

    public BotScheduler(InterviewService interviewService, UserService userService,
                        LeetCodeUtil leetCodeUtil, Manager manager, SendMessageCallback callback) {
        this.interviewService = interviewService;
        this.userService = userService;
        this.leetCodeUtil = leetCodeUtil;
        this.manager = manager;
        this.sendMessageCallback = callback;
        startReminderScheduler();
        startDailyTaskScheduler();
    }

    // Планировщик напоминалок
    private void startReminderScheduler() {
        Runnable reminderTask = () -> {
            try {
                List<Interview> interviews = interviewService.getAllInterviews();
                LocalDateTime now = LocalDateTime.now();

                for (Interview interview : interviews) {
                    LocalDateTime startTime = interview.getStart_time();
                    LocalDateTime endTime = interview.getEnd_time();

                    if (startTime == null) continue;

                    long minutesUntilStart = ChronoUnit.MINUTES.between(now, startTime);
                    if (minutesUntilStart == 60) {
                        String user1Id = interview.getPartner1Id();
                        String user2Id = interview.getPartner2Id();
                        sendMessageCallback.sendMessage(user1Id, "Привет! Не забудь про интервью через час в " +
                                startTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")), null);
                        sendMessageCallback.sendMessage(user2Id, "Привет! Не забудь про интервью через час в " +
                                startTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")), null);
                    }

                    long minutesSinceEnd = ChronoUnit.MINUTES.between(endTime, now);
                    if (minutesSinceEnd >= 0 && minutesSinceEnd <= 5) { // Запрос в течение 5 минут после окончания
                        requestRating(interview);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error in reminder task: " + e.getMessage());
                e.printStackTrace();
            }
        };

        scheduler.scheduleAtFixedRate(reminderTask, 0, 1, TimeUnit.MINUTES);
    }

    private void requestRating(Interview interview) throws TelegramApiException {
        String user1Id = interview.getPartner1Id();
        String user2Id = interview.getPartner2Id();
        User user1 = userService.getUserById(user1Id);
        User user2 = userService.getUserById(user2Id);

        // Запрос оценки для user1 (оценивает user2)
        String message1 = "Интервью с @" + user2.getTgUsername() + " завершено. " +
                "Пожалуйста, оцените вашего партнёра по 10-балльной шкале\n\n" +
                "Критерии оценки:\n" +
                "Спасибо за участие в мок-интервью! Пожалуйста, оцените вашего партнера от 1 до 10 (1 = низкая оценка, 10 = отличная). " +
                "Учитывайте следующие аспекты:\n" +
                "- Подготовленность: Насколько партнер был готов к интервью (решал задачу, объяснял решения)?\n" +
                "- Коммуникация: Был ли партнер понятен, уважителен и открыт к обсуждению?\n" +
                "- Вовлеченность: Активно ли партнер участвовал и помогал решать задачу?\n" +
                "- Пунктуальность: Присоединился ли партнер вовремя и уложился ли в запланированное время?\n" +
                "Пример: Если партнер хорошо объяснял, но опоздал, вы можете поставить 7 или 8.";
        InlineKeyboardMarkup markup1 = createRatingKeyboard(interview.getId(), user2Id);
        sendMessageCallback.sendMessage(user1Id, message1, markup1);

        // Запрос оценки для user2 (оценивает user1)
        String message2 = "Интервью с @" + user1.getTgUsername() + " завершено. " +
                "Пожалуйста, оцените вашего партнёра по 10-балльной шкале\n\n" +
                "Критерии оценки:\n" +
                "Спасибо за участие в мок-интервью! Пожалуйста, оцените вашего партнера от 1 до 10 (1 = низкая оценка, 10 = отличная). " +
                "Учитывайте следующие аспекты:\n" +
                "- Подготовленность: Насколько партнер был готов к интервью (решал задачу, объяснял решения)?\n" +
                "- Коммуникация: Был ли партнер понятен, уважителен и открыт к обсуждению?\n" +
                "- Вовлеченность: Активно ли партнер участвовал и помогал решать задачу?\n" +
                "- Пунктуальность: Присоединился ли партнер вовремя и уложился ли в запланированное время?\n" +
                "Пример: Если партнер хорошо объяснял, но опоздал, вы можете поставить 7 или 8.";
        InlineKeyboardMarkup markup2 = createRatingKeyboard(interview.getId(), user1Id);
        sendMessageCallback.sendMessage(user2Id, message2, markup2);
    }

    private InlineKeyboardMarkup createRatingKeyboard(Long interviewId, String ratedId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.valueOf(i));
            button.setCallbackData("rate_" + interviewId + "_" + ratedId + "_" + i);
            row.add(button);
            if (i % 5 == 0) {
                keyboard.add(row);
                row = new ArrayList<>();
            }
        }
        if (!row.isEmpty()) {
            keyboard.add(row);
        }
        markup.setKeyboard(keyboard);
        return markup;
    }

    // Планировщик ежедневных задач (XP и лиги)
    private void startDailyTaskScheduler() {
        Runnable dailyTask = () -> {
            try {
                LocalDate today = LocalDate.now();
                List<Interview> todayInterviews = interviewService.getInterviewsByDate(today);

                // Проверка задач и начисление XP
                for (Interview interview : todayInterviews) {
                    if (interview.getEnd_time() == null || interview.getEnd_time().isAfter(LocalDateTime.now())) {
                        continue;
                    }
                    checkTasksForInterview(interview);
                }

                // Распределение по лигам
                distributeUsersToLeagues();
            } catch (Exception e) {
                System.err.println("Error in daily task: " + e.getMessage());
                e.printStackTrace();
            }
        };

        LocalTime checkTime = LocalTime.of(22, 0);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = LocalDateTime.of(LocalDate.now(), checkTime);
        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }

        long initialDelay = ChronoUnit.SECONDS.between(now, nextRun);
        scheduler.scheduleAtFixedRate(dailyTask, initialDelay, 24 * 60 * 60, TimeUnit.SECONDS);
    }

    // Проверка задач и начисление XP
    private void checkTasksForInterview(Interview interview) throws TelegramApiException {
        String user1Id = interview.getPartner1Id();
        String user2Id = interview.getPartner2Id();
        User user1 = userService.getUserById(user1Id);
        User user2 = userService.getUserById(user2Id);

        try {
            JsonNode user1Submissions = leetCodeUtil.getUserSubmissionsAsJson(user1.getLeetcodeUsername());
            JsonNode user2Submissions = leetCodeUtil.getUserSubmissionsAsJson(user2.getLeetcodeUsername());

            int task1Xp = getXpForDifficulty(userService.getUserLeague(user1Id)); // user1 решает taskUser2
            int task2Xp = getXpForDifficulty(userService.getUserLeague(user2Id)); // user2 решает taskUser1

            boolean user1Solved = checkSubmission(user1Submissions, interview.getAssignedTaskForUser1());
            if (user1Solved) {
                long newXp = user1.getXp() + task1Xp;
                userService.updateUserXP(user1.getTelegramId(), newXp);
                sendMessageCallback.sendMessage(user1Id, "Молодец! Ты решил задачу '" + interview.getAssignedTaskForUser1() +
                        "' (" + userService.getUserLeague(user1Id) + "). Тебе начислено " + task1Xp + " XP. Новый баланс: " + newXp, null);
            } else {
                sendMessageCallback.sendMessage(user2Id, "Ты пока не решил задачу '" + interview.getAssignedTaskForUser2() +
                        "' (" + userService.getUserLeague(user2Id) + "). Больше практики — и все получится!", null);
            }

            boolean user2Solved = checkSubmission(user2Submissions, interview.getAssignedTaskForUser2());
            if (user2Solved) {
                long newXp = user2.getXp() + task2Xp;
                userService.updateUserXP(user2Id, newXp);
                sendMessageCallback.sendMessage(user2Id, "Молодец! Ты решил задачу '" + userService.getUserLeague(user1Id) +
                        "' (" + userService.getUserLeague(user1Id) + "). Тебе начислено " + task2Xp + " XP. Новый баланс: " + newXp, null);
            } else {
                sendMessageCallback.sendMessage(user2Id, "Ты пока не решил задачу '" + userService.getUserLeague(user1Id) +
                        "' (" + userService.getUserLeague(user1Id) + "). Больше практики — и все получится!", null);
            }
        } catch (Exception e) {
            System.err.println("Error checking tasks for interview " + interview.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Распределение по лигам
    private void distributeUsersToLeagues() throws TelegramApiException {
        List<User> allUsers = userService.getAllUsers();
        for (User user : allUsers) {
            long xp = user.getXp();
            String newLeague = determineLeague(xp);
            String currentLeague = user.getLeague();

            if (!newLeague.equals(currentLeague)) {
                userService.updateUserLeague(user.getTelegramId(), newLeague);
                sendMessageCallback.sendMessage(user.getTelegramId(), "Твоя лига обновлена! Новая лига: " + newLeague + " (XP: " + xp + ")", null);
            }
        }
    }

    // Определение XP по сложности
    private int getXpForDifficulty(String difficulty) {
        return switch (difficulty.toUpperCase()) {
            case "EASY" -> 10;
            case "MEDIUM" -> 20;
            case "HARD" -> 30;
            default -> 0;
        };
    }

    // Определение лиги по XP
    private String determineLeague(long xp) {
        if (xp < 700) {
            return "Easy";
        } else if (xp <= 1800) {
            return "Medium";
        } else {
            return "Hard";
        }
    }

    // Проверка submissions
    private boolean checkSubmission(JsonNode submissions, String taskSlug) {
        JsonNode submissionList = submissions.path("data").path("recentSubmissionList");
        for (JsonNode submission : submissionList) {
            String titleSlug = submission.get("titleSlug").asText();
            String status = submission.get("statusDisplay").asText();
            if (titleSlug.equals(taskSlug) && status.equals("Accepted")) {
                return true;
            }
        }
        return false;
    }

    // Остановка планировщика
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}
