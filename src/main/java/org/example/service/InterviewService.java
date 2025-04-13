package org.example.service;

import org.example.config.DatabaseConfig;
import org.example.dao.InterviewDaoImpl;
import org.example.model.Interview;
import org.example.model.User;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

    public void deleteInterview(Long id) {
        interviewDao.deleteInterview(id);
    }
}