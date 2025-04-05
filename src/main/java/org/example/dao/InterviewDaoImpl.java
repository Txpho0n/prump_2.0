package org.example.dao;

import org.example.config.DatabaseConfig;
import org.example.model.Interview;
import org.example.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InterviewDaoImpl implements InterviewDao {
    private final Connection connection;

    public InterviewDaoImpl() {
        this.connection = DatabaseConfig.getInstance().getConnection();
    }

    @Override
    public void createMockInterview(Interview interview) {
        String sql = "INSERT INTO interviews (topic_id, partner1_id, partner2_id, task_user1, task_user2, room_link, start_time, end_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, interview.getTopicId());
            ps.setString(2, interview.getPartner1Id());
            ps.setString(3, interview.getPartner2Id());
            ps.setString(4, interview.getAssignedTaskForUser1());
            ps.setString(5, interview.getAssignedTaskForUser2());
            ps.setString(6, interview.getRoomLink());
            ps.setTimestamp(7, Timestamp.valueOf(interview.getStart_time()));
            ps.setTimestamp(8, Timestamp.valueOf(interview.getEnd_time()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при создании интервью", e);
        }
    }

    @Override
    public void assignTasksToUsers(Interview interview) {
        String sql = "UPDATE interviews SET task_user1 = ?, task_user2 = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, interview.getAssignedTaskForUser1());
            ps.setString(2, interview.getAssignedTaskForUser2());
            ps.setLong(3, interview.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при назначении задач", e);
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
    public List<Interview> plannedInterviews(User user) {
        List<Interview> interviews = new ArrayList<>();
        String sql = "SELECT * FROM interviews WHERE partner1_id = ? OR partner2_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getTgUsername());
            ps.setString(2, user.getTgUsername());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Interview interview = new Interview(
                        rs.getLong("id"),
                        rs.getLong("topic_id"),
                        rs.getString("partner1_id"),
                        rs.getString("partner2_id"),
                        rs.getString("task_user1"),
                        rs.getString("task_user2"),
                        rs.getString("room_link"),
                        rs.getTimestamp("start_time").toLocalDateTime(),
                        rs.getTimestamp("end_time").toLocalDateTime()
                );
                interviews.add(interview);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении списка интервью", e);
        }
        return interviews;
    }
}
