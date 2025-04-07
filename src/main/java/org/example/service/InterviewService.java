package org.example.service;

import org.example.dao.InterviewDaoImpl;
import org.example.model.Interview;
import org.example.model.User;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

public class InterviewService {
    private final InterviewDaoImpl interviewDao;

    public InterviewService(Connection connection) throws IOException {
        this.interviewDao = new InterviewDaoImpl(connection);
    }

    public void scheduleInterview(Interview interview) {
        interviewDao.createMockInterview(interview);
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
}