package org.example.service;

import org.example.config.DatabaseConfig;
import org.example.dao.UserDaoImpl;
import org.example.model.User;

import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

public class UserService {
    private final UserDaoImpl userDao;
    private final DatabaseConfig databaseConfig = DatabaseConfig.getInstance();
    public UserService(Connection connection) throws IOException {
        this.userDao = new UserDaoImpl(connection);
    }

    public void registerUser(User user) {
        userDao.registerUser(user);
    }

    public User getUserById(String telegramId) {
        return userDao.getUserById(telegramId);
    }

    public boolean userExists(String telegramId) {
        return userDao.userExists(databaseConfig.getConnection(), telegramId);
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

    public List<User> findUsersByGroup(String league) {
        return userDao.findUsersByGroup(league);
    }
}