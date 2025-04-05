package org.example.util.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public class SolvedProblemsResponse {
    @JsonProperty("solvedProblem")
    public int solvedProblem;
    @JsonProperty("easySolved")
    public int easySolved;
    @JsonProperty("mediumSolved")
    public int mediumSolved;
    @JsonProperty("hardSolved")
    public int hardSolved;
    @JsonProperty("totalSubmissionNum")
    public List<SubmissionStats> totalSubmissionNum;
    @JsonProperty("acSubmissionNum")
    public List<SubmissionStats> acSubmissionNum;

    public static class SubmissionStats {
        public String difficulty;
        public int count;
        public int submissions;
    }
}
