package org.example.model;

import java.time.LocalDateTime;

public class User {
    private String tg_username;
    private String telegram_id;
    private String leetcode_username;
    private Long XP;
    private String league;
    private String full_name;
    private LocalDateTime last_mock_interview;
    private LocalDateTime last_solved_task_timestamp;
    private LocalDateTime registration_date;
    private boolean is_admin;

    private boolean is_active;
    private Double socialRating;


    // Constructor
    public User(String tg_username, String telegram_id, String leetcode_username, Long XP, String league, String full_name, LocalDateTime last_mock_interview, LocalDateTime last_solved_task_timestamp, LocalDateTime registration_date, boolean is_admin, boolean is_active, Double socialRating) {
        this.tg_username = tg_username;
        this.telegram_id = telegram_id;
        this.leetcode_username = leetcode_username;
        this.XP = XP;
        this.league = league;
        this.full_name = full_name;
        this.last_mock_interview = last_mock_interview;
        this.last_solved_task_timestamp = last_solved_task_timestamp;
        this.registration_date = registration_date;
        this.is_admin = is_admin;
        this.is_active = is_active;
        this.socialRating = socialRating;
    }

    // Getters
    public String getTgUsername() {
        return tg_username;
    }

    public String getTelegramId() {
        return telegram_id;
    }

    public Long getXp() {
        return XP;
    }

    public String getLeague(){
        return league;
    }

    public String getLeetcodeUsername() {
        return leetcode_username;
    }

    public String getFullName() {
        return full_name;
    }

    public LocalDateTime getLastMockInterview() {
        return last_mock_interview;
    }

    public LocalDateTime getLastSolvedTaskTimestamp() {
        return last_solved_task_timestamp;
    }

    public LocalDateTime getRegistrationDate() {
        return registration_date;
    }

    public boolean isAdmin() {
        return is_admin;
    }
    public boolean isActive() {
        return is_active;
    }

    // Setters
    public void setTgUsername(String tg_username) {
        this.tg_username = tg_username;
    }

    public void setTelegramId(String telegram_id) {
        this.telegram_id = telegram_id;
    }

    public void setXp(Long XP) {
        this.XP = XP;
    }

    public void setLeague(String league){
        this.league = league;
    }

    public void setLeetcodeUsername(String leetcode_username) {
        this.leetcode_username = leetcode_username;
    }

    public void setFullName(String full_name) {
        this.full_name = full_name;
    }

    public void setLastMockInterview(LocalDateTime last_mock_interview) {
        this.last_mock_interview = last_mock_interview;
    }

    public void setLastSolvedTaskTimestamp(LocalDateTime last_solved_task_timestamp) {
        this.last_solved_task_timestamp = last_solved_task_timestamp;
    }

    public void setRegistrationDate(LocalDateTime registration_date) {
        this.registration_date = registration_date;
    }

    public void setAdmin(boolean is_admin) {
        this.is_admin = is_admin;
    }
    public void setActive(boolean is_active) {
        this.is_active = is_active;
    }

    public Double getSocialRating() { return socialRating; }
    public void setSocialRating(Double socialRating) { this.socialRating = socialRating; }
}