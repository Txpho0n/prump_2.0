package org.example.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.BotConfig;
import org.example.dao.InterviewDaoImpl;
import org.example.dao.UserDaoImpl;
import org.example.model.User;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

public class Manager {
    private final LeetCodeUtil client = new LeetCodeUtil();
    private final UserDaoImpl userDao;
    private final InterviewDaoImpl interviewDao;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Manager(Connection connection) throws IOException {
        this.interviewDao = new InterviewDaoImpl(connection);
        this.userDao = new UserDaoImpl(connection);
    }

    public JsonNode matchTask(Long xp_points) throws IOException, InterruptedException {
        String taskDifficulty;
        if (xp_points < 1000) {
            taskDifficulty = "Easy";
        } else if (xp_points < 2000) {
            taskDifficulty = "Medium";
        } else {
            taskDifficulty = "Hard";
        }
        String topic = BotConfig.getTopic();
        System.out.println("Matching task for topic: " + topic + ", difficulty: " + taskDifficulty);

        // Получаем задачи с минимальным лимитом, чтобы проверить наличие
        JsonNode problemsResponse = client.getProblemsAsJson(10, topic, 0, null);
        JsonNode questions = problemsResponse.path("data").path("problemsetQuestionList").path("questions");

        if (!questions.isArray() || questions.size() == 0) {
            System.err.println("No tasks found for topic: " + topic + ", difficulty: " + taskDifficulty);
            // Запасной вариант: пробуем без темы
            problemsResponse = client.getProblemsAsJson(10, null, 0, null);
            questions = problemsResponse.path("data").path("problemsetQuestionList").path("questions");
            if (!questions.isArray() || questions.size() == 0) {
                throw new IOException("Не удалось найти подходящую задачу для сложности " + taskDifficulty);
            }
        }

        // Выбираем случайную задачу из полученного списка
        int randomIndex = (int) (Math.random() * questions.size());
        JsonNode selectedTask = questions.get(randomIndex);
        System.out.println("Selected task: " + selectedTask.toString());

        return selectedTask;
    }

    public String getPeerTelegramId(String league) {
        List<User> users = userDao.findUsersByGroup(league);
        if (users.isEmpty()) return null;

        List<User> activeUsers = users.stream()
                .filter(user -> userDao.isActive(user.getTelegramId()))
                .toList();

        if (activeUsers.isEmpty()) return null;
        int random = (int) (Math.random() * activeUsers.size());
        return activeUsers.get(random).getTelegramId();
    }

    public void initialAssessment(String leetcodeUsername) throws IOException, InterruptedException {
        JsonNode solvedProblems = client.getUserSolvedProblemsAsJson(leetcodeUsername);

        JsonNode submitStats = solvedProblems.path("data").path("matchedUser").path("submitStats").path("acSubmissionNum");
        if (submitStats.isMissingNode() || !submitStats.isArray()) {
            throw new IOException("Не удалось получить статистику решенных задач для " + leetcodeUsername);
        }

        long easySolved = 0, mediumSolved = 0, hardSolved = 0;
        for (JsonNode stat : submitStats) {
            String difficulty = stat.get("difficulty").asText();
            long count = stat.get("count").asLong();
            switch (difficulty) {
                case "Easy":
                    easySolved = count;
                    break;
                case "Medium":
                    mediumSolved = count;
                    break;
                case "Hard":
                    hardSolved = count;
                    break;
            }
        }

        long totalXP = easySolved * 10 + mediumSolved * 20 + hardSolved * 30;

        User user = userDao.getUserByLeetCodeUsername(leetcodeUsername);
        if (user != null) {
            userDao.updateUserXP(user.getTelegramId(), totalXP);
        } else {
            throw new IllegalStateException("Пользователь с LeetCode именем " + leetcodeUsername + " не найден в базе");
        }
    }
}