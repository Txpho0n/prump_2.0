package org.example.service;

import org.example.config.DatabaseConfig;
import org.example.dao.InterviewDaoImpl;
import org.example.model.Interview;
import org.example.model.User;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;





public class InterviewService {
    private final InterviewDaoImpl interviewDao;
    private final DatabaseConfig databaseConfig = DatabaseConfig.getInstance();

    public InterviewService() throws IOException {
        this.interviewDao = new InterviewDaoImpl(databaseConfig);
    }

    public void updateInterview(Interview interview) {
        interviewDao.updateInterview(interview);
    }

    public Long scheduleInterview(Interview interview) {
        return interviewDao.createMockInterview(interview);
    }

    public List<Interview> getInterviewsByDate(LocalDate date) {
        return interviewDao.getInterviewsByDate(date);
    }

    public String getUser1Task(Interview interview) {
        return interviewDao.getUser1Task(interview);
    }

    public String getUser2Task(Interview interview) {
        return interviewDao.getUser2Task(interview);
    }

    public String getUser1(Interview interview) {
        return interviewDao.getUser1(interview);
    }

    public String getUser2(Interview interview) {
        return interviewDao.getUser2(interview);
    }

    public void updateTasks(Interview interview) {
        interviewDao.assignTasksToUsers(interview);
    }

    public List<Interview> getUpcomingInterviews(User user) {
        return interviewDao.plannedInterviews(user);
    }

    public Interview getInterviewById(Long id) {
        return interviewDao.getInterviewById(id);
    }

    public List<Interview> getAllInterviews() {
        return interviewDao.getAllInterviews();
    }
    public Long findAllActiveInterviewsAmount(String telegramId) {
        return interviewDao.findAllActiveInterviewsAmount(telegramId);
    }
    public List<Interview> findAllActiveInterviewsByTgId(String telegramId){
        return interviewDao.findAllActiveInterviewsByTgId(telegramId);
    }
    public void deleteInterview(Long id) {
        interviewDao.deleteInterview(id);
    }


    public int getTotalInterviewsCount() {
        try (Connection conn = databaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM interviews")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error getting total interviews count", e);
        }
    }

    public int getCompletedInterviewsCount() {
        try (Connection conn = databaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM interviews WHERE end_time < NOW()")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error getting completed interviews count", e);
        }
    }

    public int getActiveInterviewsCount() {
        try (Connection conn = databaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM interviews WHERE end_time >= NOW()")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error getting active interviews count", e);
        }
    }

    public int getInterviewsInLastHours(int hours) {
        try (Connection conn = databaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM interviews WHERE start_time >= NOW() - INTERVAL '" + hours + " hours'")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error getting interviews in last hours", e);
        }
    }

    public int getInterviewsInLastDays(int days) {
        try (Connection conn = databaseConfig.getConnection();
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM interviews WHERE start_time >= NOW() - INTERVAL '" + days + " days'")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error getting interviews in last days", e);
        }
    }


}