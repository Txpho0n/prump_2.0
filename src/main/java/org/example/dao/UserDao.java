package org.example.dao;

import org.example.model.User;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

public interface UserDao {
    void registerUser(User user);
    User getUserById(String tg_username);
    User getUserByLeetCodeUsername(String leet_code_username);

    void updateUserXP(String tg_username, Long new_xp);

    void updateLastSolvedTaskTimestamp(String tg_username, LocalDateTime new_timestamp);

    void updateLeetCodeUsername(String telegramId, String leetcodeUsername);

    void updateLastMockTimestamp(String tg_username, LocalDateTime new_timestamp);

    List<User> findUsersByGroup(String league);
    boolean userExists(Connection connection, String telegramId);

}
