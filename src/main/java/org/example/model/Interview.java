package org.example.model;

import java.time.LocalDateTime;

public class Interview {
    private Long id;
    private Long topicId;
    private String partner1Id;  // TG username партнёра 1
    private String partner2Id;
    private String assignedTaskForUser1;
    private String assignedTaskForUser2;
    private String roomLink;
    private LocalDateTime start_time;
    private LocalDateTime end_time;

}
