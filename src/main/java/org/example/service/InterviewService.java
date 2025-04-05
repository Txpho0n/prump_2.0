package org.example.service;

import org.example.model.Interview;
import org.example.model.User;
import java.util.List;

public interface InterviewService {
    void scheduleInterview(Interview interview);
    void updateTasks(Interview interview);
    List<Interview> getUpcomingInterviews(User user);
}