package org.example.dao;

import org.example.model.Interview;
import org.example.model.User;

import java.util.List;

public interface InterviewDao {
    Long createMockInterview(Interview interview);
    Interview getInterviewById(Long id);
    void assignTasksToUsers(Interview interview);
    String getUser1Task(Interview interview);
    String  getUser2Task(Interview interview);
    String getUser1(Interview interview);
    String  getUser2(Interview interview);
    List<Interview> plannedInterviews(User user);
    public List<Interview> getAllInterviews();
}
