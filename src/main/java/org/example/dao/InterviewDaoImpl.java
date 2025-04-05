package org.example.dao;

import org.example.model.Interview;
import org.example.model.User;

import java.util.List;

public class InterviewDaoImpl implements InterviewDao {
    @Override
    public void createMockInterview(Interview interview) {

    }

    @Override
    public void assignTasksToUsers(Interview interview) {

    }

    @Override
    public String getUser1Task(Interview interview) {
        return "";
    }

    @Override
    public String getUser2Task(Interview interview) {
        return "";
    }

    @Override
    public List<Interview> plannedInterviews(User user) {
        return List.of();
    }
}
