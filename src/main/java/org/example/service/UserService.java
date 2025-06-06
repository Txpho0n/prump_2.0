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

    public int getTotalUsersCount() {
        try (Connection conn = dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error getting total users count", e);
        }
    }

    public int getActiveUsersCount() {
        try (Connection conn = dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE is_active = true")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error getting active users count", e);
        }
    }

    public int getUsersWithLeetcodeCount() {
        try (Connection conn = dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM users WHERE leetcode_username IS NOT NULL")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error getting users with leetcode count", e);
        }
    }

    public int getUsersActiveInLastHours(int hours) {
        try (Connection conn = dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(DISTINCT u.telegram_id) FROM users u " +
                "JOIN interviews i ON u.telegram_id = i.partner1_id OR u.telegram_id = i.partner2_id " +
                "WHERE i.start_time >= NOW() - INTERVAL '" + hours + " hours'")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error getting users active in last hours", e);
        }
    }

    public int getUsersActiveInLastDays(int days) {
        try (Connection conn = dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(DISTINCT u.telegram_id) FROM users u " +
                "JOIN interviews i ON u.telegram_id = i.partner1_id OR u.telegram_id = i.partner2_id " +
                "WHERE i.start_time >= NOW() - INTERVAL '" + days + " days'")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error getting users active in last days", e);
        }
    }

    public Map<String, Integer> getLeagueDistribution() {
        Map<String, Integer> distribution = new HashMap<>();
        
        try (Connection conn = dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT league, COUNT(*) as count FROM users GROUP BY league")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                distribution.put(rs.getString("league"), rs.getInt("count"));
            }
            return distribution;
        } catch (SQLException e) {
            throw new RuntimeException("Error getting league distribution", e);
        }
    }

    public double getAverageRating() {
        try (Connection conn = dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT AVG(rating) FROM ratings")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        } catch (SQLException e) {
            throw new RuntimeException("Error getting average rating", e);
        }
    }

    public int getTotalRatingsCount() {
        try (Connection conn = dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM ratings")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error getting total ratings count", e);
        }
    }

    public Map<Integer, Integer> getRatingDistribution() {
        Map<Integer, Integer> distribution = new HashMap<>();
        
        try (Connection conn = dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT rating, COUNT(*) as count FROM ratings GROUP BY rating")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                distribution.put(rs.getInt("rating"), rs.getInt("count"));
            }
            return distribution;
        } catch (SQLException e) {
            throw new RuntimeException("Error getting rating distribution", e);
        }
    }

    public List<User> getRecentUsers(int days) {
        List<User> users = new ArrayList<>();
        
        try (Connection conn = dbConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM users WHERE registration_date >= NOW() - INTERVAL '" + days + " days' " +
                "ORDER BY registration_date DESC")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
            return users;
        } catch (SQLException e) {
            throw new RuntimeException("Error getting recent users", e);
        }
    }
}