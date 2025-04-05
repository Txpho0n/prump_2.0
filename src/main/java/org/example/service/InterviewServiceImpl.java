package org.example.service;

import org.example.dao.InterviewDao;
import org.example.model.Interview;
import org.example.model.User;
import java.util.List;

public class InterviewServiceImpl implements InterviewService {
    private final InterviewDao interviewDao;

    public InterviewServiceImpl(InterviewDao interviewDao) {
        this.interviewDao = interviewDao;
    }

    @Override
    public void scheduleInterview(Interview interview) {
        interviewDao.createMockInterview(interview);
    }

    @Override
    public void updateTasks(Interview interview) {
        interviewDao.assignTasksToUsers(interview);
    }

    @Override
    public List<Interview> getUpcomingInterviews(User user) {
        return interviewDao.plannedInterviews(user);
    }
}