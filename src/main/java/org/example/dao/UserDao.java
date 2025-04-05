package org.example.dao;

import org.example.model.User;

import java.sql.Connection;

public interface UserDao {
    void registerUser(User user);
    User getUserById(String tg_username);
    void updateUserXP(String tg_username);
    void updateLastSolvedTaskTimestamp(String tg_username);
    void updateLastMockTimestamp(String tg_username);
    void findUserByGroup(User user);
    boolean userExists(Connection connection, String telegramId);
}
