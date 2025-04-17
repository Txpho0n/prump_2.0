package org.example.model;


import java.time.LocalDateTime;

public class Rating {
    private Long id;
    private String raterId;
    private String ratedId;
    private Long interviewId;
    private Integer rating;
    private LocalDateTime createdAt;

    // Конструктор
    public Rating(String raterId, String ratedId, Long interviewId, Integer rating) {
        this.raterId = raterId;
        this.ratedId = ratedId;
        this.interviewId = interviewId;
        this.rating = rating;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRaterId() { return raterId; }
    public void setRaterId(String raterId) { this.raterId = raterId; }
    public String getRatedId() { return ratedId; }
    public void setRatedId(String ratedId) { this.ratedId = ratedId; }
    public Long getInterviewId() { return interviewId; }
    public void setInterviewId(Long interviewId) { this.interviewId = interviewId; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}