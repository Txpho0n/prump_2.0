package org.example.dao;

import org.example.model.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDaoImpl implements UserDao, DaoInterface {

    private final Connection connection;

    @Override
    public void createTableIfNotExists() {
        String schemaPath = "src/main/resources/sql/users_schema.sql"; // Путь к файлу с SQL-скриптом

        try (BufferedReader reader = new BufferedReader(new FileReader(schemaPath));
             Statement stmt = connection.createStatement()) {

            StringBuilder sql = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sql.append(line).append("\n");
            }

            stmt.executeUpdate(sql.toString());
            System.out.println("✅ Таблица users проверена/создана!");

        } catch (SQLException | IOException e) {
            System.out.println("⚠ Ошибка при создании таблицы users: " + e.getMessage());
        }
    }

    public UserDaoImpl(Connection connection) throws IOException {
        this.connection = connection;
        createTableIfNotExists();
    }

    @Override
    public void registerUser(User user) {
        String sql = "INSERT INTO users (telegram_id, tg_username, xp, full_name, last_mock_interview, last_solved_task_timestamp, registration_date, is_admin) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getTelegramId());
            stmt.setString(2, user.getTgUsername());
            stmt.setLong(3, user.getXp());
            stmt.setString(4, user.getFullName());

            stmt.setTimestamp(5, user.getLastMockInterview() != null
                    ? Timestamp.valueOf(user.getLastMockInterview())
                    : null);

            stmt.setTimestamp(6, user.getLastSolvedTaskTimestamp() != null
                    ? Timestamp.valueOf(user.getLastSolvedTaskTimestamp())
                    : null);

            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

            stmt.setBoolean(8, user.isAdmin());

            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public User getUserById(String telegram_id) {
        String sql = "SELECT * FROM users WHERE telegram_id = ?";
        try(PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, telegram_id);
            ResultSet rs = stmt.executeQuery();
            if(rs.next()){
                return new User(
                        rs.getString("tg_username"),
                        rs.getString("leetcode_username"),
                        rs.getString("league"),
                        rs.getString("telegram_id"),
                        rs.getLong("xp"),
                        rs.getString("full_name"),
                        rs.getTimestamp("last_mock_interview") != null
                                ? rs.getTimestamp("last_mock_interview").toLocalDateTime()
                                : null,
                        rs.getTimestamp("last_solved_task_timestamp") != null
                                ? rs.getTimestamp("last_solved_task_timestamp").toLocalDateTime()
                                : null,
                        rs.getTimestamp("registration_date").toLocalDateTime(),
                        rs.getBoolean("is_admin")
                );
            } else {
                return null;
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public User getUserByLeetCodeUsername(String leetcodeUsername) {
        String sql = "SELECT * FROM users WHERE leetcode_username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, leetcodeUsername);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getString("tg_username"),
                        rs.getString("leetcode_username"),
                        rs.getString("league"),
                        rs.getString("telegram_id"),
                        rs.getLong("xp"),
                        rs.getString("full_name"),
                        rs.getTimestamp("last_mock_interview") != null
                                ? rs.getTimestamp("last_mock_interview").toLocalDateTime()
                                : null,
                        rs.getTimestamp("last_solved_task_timestamp") != null
                                ? rs.getTimestamp("last_solved_task_timestamp").toLocalDateTime()
                                : null,
                        rs.getTimestamp("registration_date").toLocalDateTime(),
                        rs.getBoolean("is_admin")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean userExists(Connection connection, String telegramId) {
        String sql = "SELECT COUNT(*) FROM users WHERE telegram_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, telegramId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void updateUserXP(String telegramId, Long new_xp) {
        String sql = "UPDATE users SET xp = ? WHERE telegram_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, new_xp);
            stmt.setString(2, telegramId);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                System.err.println("⚠ Пользователь с telegram_id=" + telegramId + " не найден");
            } else {
                System.out.println("✅ XP пользователя " + telegramId + " обновлен: " + new_xp);
            }
        } catch (SQLException e) {
            System.err.println("❌ Ошибка при обновлении XP: " + e.getMessage());
        }
    }

    @Override
    public void updateLastSolvedTaskTimestamp(String tg_username, LocalDateTime new_timestamp) {
        String sql = "UPDATE users SET last_solved_task_timestamp = ? WHERE tg_username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setTimestamp(1, Timestamp.valueOf(new_timestamp));
            stmt.setString(2, tg_username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateLeetCodeUsername(String telegramId, String leetcodeUsername) {
        String sql = "UPDATE users SET leetcode_username = ? WHERE telegram_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, leetcodeUsername);
            stmt.setString(2, telegramId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateLastMockTimestamp(String tg_username, LocalDateTime new_timestamp) {
        String sql = "UPDATE users SET last_mock_interview = ? WHERE tg_username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setTimestamp(1, Timestamp.valueOf(new_timestamp));
            stmt.setString(2, tg_username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<User> findUsersByGroup(String league) {
        String sql = "SELECT * FROM users WHERE league = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setString(1, league);
            ResultSet rs = stmt.executeQuery();
            List<User> users = new ArrayList<>();
            while (rs.next()){
                users.add(new User(
                        rs.getString("tg_username"),
                        rs.getString("leetcode_username"),
                        rs.getString("league"),
                        rs.getString("telegram_id"),
                        rs.getLong("xp"),
                        rs.getString("full_name"),
                        rs.getTimestamp("last_mock_interview") != null
                                ? rs.getTimestamp("last_mock_interview").toLocalDateTime()
                                : null,
                        rs.getTimestamp("last_solved_task_timestamp") != null
                                ? rs.getTimestamp("last_solved_task_timestamp").toLocalDateTime()
                                : null,
                        rs.getTimestamp("registration_date").toLocalDateTime(),
                        rs.getBoolean("is_admin")
                ));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
