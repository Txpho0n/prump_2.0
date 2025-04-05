package org.example.util.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class CalendarResponse {
    @JsonProperty("submissionCalendar")
    public Map<String, Integer> submissionCalendar;
}
