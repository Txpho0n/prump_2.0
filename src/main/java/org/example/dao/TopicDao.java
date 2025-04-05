package org.example.dao;

import org.example.model.Topic;

import java.util.List;

public interface TopicDao {
    void addTopic(Topic topic);
    List<Topic> findAllTopics();
}
