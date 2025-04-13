package org.example.dao;

import org.example.config.DatabaseConfig;
import org.example.model.Interview;
import org.example.model.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InterviewDaoImpl implements InterviewDao {
    private static DatabaseConfig dbConfig;



    public InterviewDaoImpl(DatabaseConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    @Override
    public Long createMockInterview(Interview interview) {
        String sql = "INSERT INTO interviews (partner1_id, partner2_id, task_user1, task_user2, start_time, end_time) " +
                "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection connection = dbConfig.getConnection(); PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, interview.getPartner1Id());
            ps.setString(2, interview.getPartner2Id());
            ps.setString(3, interview.getAssignedTaskForUser1());
            ps.setString(4, interview.getAssignedTaskForUser2());
            ps.setObject(5, interview.getStart_time() != null ? Timestamp.valueOf(interview.getStart_time()) : null);
            ps.setObject(6, interview.getEnd_time() != null ? Timestamp.valueOf(interview.getEnd_time()) : null);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving interview", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return 0L;
    }
    public Interview getInterviewById(Long id) {
        String sql = "SELECT * FROM interviews WHERE id = ?";
        try (Connection connection = dbConfig.getConnection();PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Interview(
                        rs.getLong("id"),
                        rs.getString("partner1_id"),
                        rs.getString("partner2_id"),
                        rs.getString("task_user1"),
                        rs.getString("task_user2"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time").toLocalDateTime()

                );
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении интервью", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }


    public void deleteInterview(Long id) {
        try (Connection connection = dbConfig.getConnection();PreparedStatement stmt = connection.prepareStatement("DELETE FROM interviews WHERE id = ?")) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete interview: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void updateInterview(Interview interview) {
        String sql = "UPDATE interviews SET partner1_id = ?, partner2_id = ?, task_user1 = ?, task_user2 = ?, start_time = ?, end_time = ? WHERE id = ?";
        try (Connection connection = dbConfig.getConnection();PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, interview.getPartner1Id());
            ps.setString(2, interview.getPartner2Id());
            ps.setString(3, interview.getAssignedTaskForUser1());
            ps.setString(4, interview.getAssignedTaskForUser2());
            ps.setTimestamp(5, Timestamp.valueOf(interview.getStart_time()));
            ps.setTimestamp(6, Timestamp.valueOf(interview.getEnd_time()));
            ps.setLong(7, interview.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating interview", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void assignTasksToUsers(Interview interview) {
        String sql = "UPDATE interviews SET task_user1 = ?, task_user2 = ? WHERE id = ?";
        try (Connection connection = dbConfig.getConnection();PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, interview.getAssignedTaskForUser1());
            ps.setString(2, interview.getAssignedTaskForUser2());
            ps.setLong(3, interview.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при назначении задач", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getUser1Task(Interview interview) {
        return interview.getAssignedTaskForUser1();
    }

    @Override
    public String getUser2Task(Interview interview) {
        return interview.getAssignedTaskForUser2();
    }

    @Override
    public String getUser1(Interview interview) {
        String sql = "SELECT partner1_id FROM interviews WHERE id = ?";
        try (Connection connection = dbConfig.getConnection();PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, interview.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("partner1_id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении пользователя 1", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    @Override
    public String getUser2(Interview interview) {
        String sql = "SELECT partner2_id FROM interviews WHERE id = ?";
        try (Connection connection = dbConfig.getConnection();PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, interview.getId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("partner2_id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении пользователя 2", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return "";
    }


    public List<Interview> getInterviewsByDate(LocalDate date) {
        String sql = "SELECT * FROM interviews WHERE start_time >= ? AND start_time < ?";
        try (Connection connection = dbConfig.getConnection();PreparedStatement ps = connection.prepareStatement(sql)) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            ps.setTimestamp(1, Timestamp.valueOf(startOfDay));
            ps.setTimestamp(2, Timestamp.valueOf(endOfDay));
            ResultSet rs = ps.executeQuery();
            List<Interview> interviews = new ArrayList<>();
            while (rs.next()) {
                interviews.add(new Interview(
                        rs.getLong("id"),
                        rs.getString("partner1_id"),
                        rs.getString("partner2_id"),
                        rs.getString("task_user1"),
                        rs.getString("task_user2"),
                        rs.getTimestamp("start_time") != null ? rs.getTimestamp("start_time").toLocalDateTime() : null,
                        rs.getTimestamp("end_time") != null ? rs.getTimestamp("end_time").toLocalDateTime() : null
                ));
            }
            return interviews;
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching interviews by date", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Interview> plannedInterviews(User user) {
        List<Interview> interviews = new ArrayList<>();
        String sql = "SELECT * FROM interviews WHERE partner1_id = ? OR partner2_id = ?";
        try (Connection connection = dbConfig.getConnection();PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getTgUsername());
            ps.setString(2, user.getTgUsername());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Interview interview = new Interview(
                        rs.getLong("id"),
                        rs.getString("partner1_id"),
                        rs.getString("partner2_id"),
                        rs.getString("task_user1"),
                        rs.getString("task_user2"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time").toLocalDateTime()
                );
                interviews.add(interview);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении списка интервью", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return interviews;
    }

    @Override
    public List<Interview> getAllInterviews(){
        List<Interview> interviews = new ArrayList<>();
        String sql = "SELECT * FROM interviews";
        try (Connection connection = dbConfig.getConnection();PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Interview interview = new Interview(
                        rs.getLong("id"),
                        rs.getString("partner1_id"),
                        rs.getString("partner2_id"),
                        rs.getString("task_user1"),
                        rs.getString("task_user2"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time").toLocalDateTime()
                );
                interviews.add(interview);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении всех интервью", e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return interviews;
    }

}
