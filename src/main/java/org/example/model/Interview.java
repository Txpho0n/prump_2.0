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

    //Constructor
    public Interview(Long id, String partner1Id, String partner2Id, String assignedTaskForUser1, String assignedTaskForUser2, LocalDateTime start_time, LocalDateTime end_time) {
        this.id = id;
        this.partner1Id = partner1Id;
        this.partner2Id = partner2Id;
        this.assignedTaskForUser1 = assignedTaskForUser1;
        this.assignedTaskForUser2 = assignedTaskForUser2;
        this.start_time = start_time;
        this.end_time = end_time;
    }

    //Getters
    public Long getId() {return id;}
    public String getPartner1Id() {return partner1Id;}
    public String getPartner2Id() {return partner2Id;}
    public String getAssignedTaskForUser1() {return assignedTaskForUser1;}
    public String getAssignedTaskForUser2() {return assignedTaskForUser2;}
    public LocalDateTime getStart_time() {return start_time;}
    public LocalDateTime getEnd_time() {return end_time;}

   //Setters
    public void setId(Long id) {this.id = id;}
    public void setPartner1Id(String partner1Id) {this.partner1Id = partner1Id;}
    public void setPartner2Id(String partner2Id) {this.partner2Id = partner2Id;}
    public void setAssignedTaskForUser1(String assignedTaskForUser1){this.assignedTaskForUser1 = assignedTaskForUser1;}
    public void setAssignedTaskForUser2(String assignedTaskForUser2){this.assignedTaskForUser2 = assignedTaskForUser2;}
    public void setStart_time(LocalDateTime start_time) {this.start_time = start_time;}
    public void setEnd_time(LocalDateTime end_time) {this.end_time = end_time;}
}
