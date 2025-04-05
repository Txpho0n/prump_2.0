package org.example.util.responses;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ProblemsResponse {
    @JsonProperty("totalQuestions")
    public int totalQuestions;
    @JsonProperty("problemsetQuestionList")
    public List<Problem> problemsetQuestionList;

    public static class Problem {
        public String title;
        @JsonProperty("titleSlug")
        public String titleSlug;
        public String difficulty;
        public List<Tag> topicTags;

        public static class Tag {
            public String name;
            public String slug;
        }
    }
}
