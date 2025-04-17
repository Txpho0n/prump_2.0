package org.example.service;

import org.example.config.DatabaseConfig;
import org.example.dao.UserDaoImpl;
import org.example.model.Rating;
import org.example.model.User;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.List;

public class UserService {
    private final UserDaoImpl userDao;
    private final DatabaseConfig databaseConfig = DatabaseConfig.getInstance();

    public UserService() throws IOException {
        this.userDao = new UserDaoImpl(databaseConfig);
    }

    public void registerUser(User user) {
        userDao.registerUser(user);
    }

    public User getUserById(String telegramId) {
        return userDao.getUserById(telegramId);
    }


    public boolean isActive(String telegramId) {
        return userDao.isActive(telegramId);
    }

    public void setActive(String telegramId, boolean isActive) {
        userDao.setActive(telegramId, isActive);
    }

    public boolean isAdmin(String telegramId) {
        return userDao.isUserAdmin(telegramId);
    }

    public boolean userExists(String telegramId) {
        return userDao.userExists(telegramId);
    }

    public void updateUserXP(String tgUsername, Long newXP) {
        userDao.updateUserXP(tgUsername, newXP);
    }

    public void updateLastSolvedTaskTimestamp(String tgUsername, LocalDateTime newTimestamp) {
        userDao.updateLastSolvedTaskTimestamp(tgUsername, newTimestamp);
    }

    public void updateLastMockTimestamp(String tgUsername, LocalDateTime newTimestamp) {
        userDao.updateLastMockTimestamp(tgUsername, newTimestamp);
    }

    public void saveRating(Rating rating) {
        userDao.saveRating(rating);
    }

    public void updateSocialRating(String telegramId) {
        userDao.updateSocialRating(telegramId);
    }

    public String getUserLeague(String telegramId) {
        return userDao.getUserLeague(telegramId);
    }

    public void updateLeetCodeUsername(String telegramId, String leetcodeUsername) {
        userDao.updateLeetCodeUsername(telegramId, leetcodeUsername);
    }

    public List<User> findAllUsers(){
        return userDao.findAllUsers();
    }

    public List<User> findUsersByGroup(String league) {
        return userDao.findUsersByGroup(league);
    }

    public void updateUserLeague(String telegramId, String newLeague) {
        userDao.updateUserLeague(telegramId, newLeague);
    }

    public List<User> getAllUsers() {
        return userDao.findAllUsers();
    }
}