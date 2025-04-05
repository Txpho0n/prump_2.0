package org.example.dao;

import org.example.model.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;

public class UserDaoImpl implements UserDao {

    private final Connection connection;

    public void createTableIfNotExists() {
        String schemaPath = "src/main/resources/sql/postgresql_schema.sql"; // Путь к файлу с SQL-скриптом

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
    public void updateUserXP(String tg_username) {

    }

    @Override
    public void updateLastSolvedTaskTimestamp(String tg_username) {

    }

    @Override
    public void updateLastMockTimestamp(String tg_username) {

    }

    @Override
    public void findUserByGroup(User user) {

    }
}
