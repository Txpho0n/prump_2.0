package org.example.dao;

import org.example.model.Interview;
import org.example.model.User;

import java.util.List;

public interface InterviewDao {
    void createMockInterview(Interview interview);
    void assignTasksToUsers(Interview interview);
    String getUser1Task(Interview interview);
    String  getUser2Task(Interview interview);
    List<Interview> plannedInterviews(User user);

}
