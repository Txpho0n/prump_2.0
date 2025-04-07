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
    private LeetCodeUtil client = new LeetCodeUtil();
    private final UserDaoImpl userDao;
    private final InterviewDaoImpl interviewDao;

    public Manager(Connection connection) throws IOException {
        this.interviewDao = new InterviewDaoImpl(connection);
        this.userDao = new UserDaoImpl(connection);
    }


    public JsonNode matchTask(Long xp_points) throws IOException {
        String taskDifficulty = null;
        if (xp_points < 1000) {
            taskDifficulty = "EASY";
        } else if (xp_points < 2000) {
            taskDifficulty = "MEDIUM";
        } else {
            taskDifficulty = "HARD";
        }
        String topic = BotConfig.getTopic();
        int totalProblems = client.getProblemsAsJson(1, topic, null, taskDifficulty).get("totalQuestions").asInt();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode firstProblem = null;

        while (true) {
            int randomNumber = (int) (Math.random() * totalProblems);
            String response = String.valueOf(client.getProblemsAsJson(randomNumber, topic, 1, taskDifficulty));
            JsonNode rootNode = objectMapper.readTree(response);
            JsonNode problemsetQuestionList = rootNode.get("problemsetQuestionList");
            if (problemsetQuestionList.isArray() && problemsetQuestionList.size() > 0) {
                firstProblem = problemsetQuestionList.get(0);
                if (!firstProblem.get("isPaidOnly").asBoolean()) {
                    break;
                }
            }
        }

        /*
        получаем поля задачи

        double acRate = firstProblem.get("acRate").asDouble();
        String difficulty = firstProblem.get("difficulty").asText();
        String questionFrontendId = firstProblem.get("questionFrontendId").asText();
        boolean isFavor = firstProblem.get("isFavor").asBoolean();
        String title = firstProblem.get("title").asText();
        String titleSlug = firstProblem.get("titleSlug").asText();

        */

        return firstProblem;
    }

    public String getPeerTelegramId(String league){
        List<User> users = userDao.findUsersByGroup(league);
        int random = (int) (Math.random() * users.size());
        return users.get(random).getTelegramId();
    }

    public void initialAssessment(String leetcodeUsername) throws IOException {
        JsonNode solvedProblems = client.getUserSolvedProblemsAsJson(leetcodeUsername);

        if (solvedProblems == null || solvedProblems.isNull()) {
            throw new IOException("Не удалось получить данные из LeetCode");
        }

        long easySolved = solvedProblems.get("easySolved").asLong();
        long mediumSolved = solvedProblems.get("mediumSolved").asLong();
        long hardSolved = solvedProblems.get("hardSolved").asLong();

        long totalXP = easySolved * 10 + mediumSolved * 20 + hardSolved * 30;

        User user = userDao.getUserByLeetCodeUsername(leetcodeUsername);
        if (user != null) {
            userDao.updateUserXP(user.getTelegramId(), totalXP);
        }
    }
}
