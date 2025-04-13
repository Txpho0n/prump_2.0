package org.example.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeetCodeUtil {
    private static final String GRAPHQL_URL = "https://leetcode.com/graphql";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public JsonNode getProblemsAsJson(Integer limit, String tags, Integer skip, String difficulty) throws IOException, InterruptedException {
        String query = """
            query problemsetQuestionList($categorySlug: String!, $limit: Int, $skip: Int, $filters: QuestionListFilterInput) {
                problemsetQuestionList: questionList(
                    categorySlug: $categorySlug
                    limit: $limit
                    skip: $skip
                    filters: $filters
                ) {
                    questions: data {
                        title
                        titleSlug
                        difficulty
                        topicTags {
                            name
                        }
                    }
                }
            }
        """;

        Map<String, Object> filters = new HashMap<>();
        if (tags != null && !tags.isEmpty()) {
            filters.put("tags", List.of(tags)); // Изменено на List.of(tags)
        }
        if (difficulty != null) {
            filters.put("difficulty", difficulty.toUpperCase());
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("categorySlug", "all-code-essentials");
        if (limit != null) {
            variables.put("limit", limit);
        }
        if (skip != null) {
            variables.put("skip", skip);
        }
        variables.put("filters", filters);

        JsonNode response = executeGraphQLQuery(query, variables);
        System.out.println("LeetCode API response: " + response.toString()); // Лог ответа
        return response;
    }

    public JsonNode getUserSolvedProblemsAsJson(String username) throws IOException, InterruptedException {
        String query = """
            query userProblemsSolved($username: String!) {
                matchedUser(username: $username) {
                    submitStats: submitStatsGlobal {
                        acSubmissionNum {
                            difficulty
                            count
                        }
                    }
                }
            }
        """;
        Map<String, Object> variables = Map.of("username", username);
        return executeGraphQLQuery(query, variables);
    }

    public JsonNode getUserSubmissionsAsJson(String username) throws IOException, InterruptedException {
        String query = """
            query recentSubmissions($username: String!, $limit: Int!) {
                recentSubmissionList(username: $username, limit: $limit) {
                    title
                    titleSlug
                    timestamp
                    statusDisplay
                    lang
                }
            }
        """;
        Map<String, Object> variables = Map.of(
                "username", username,
                "limit", 20
        );
        return executeGraphQLQuery(query, variables);
    }

    public JsonNode getUserCalendarAsJson(String username) throws IOException, InterruptedException {
        String query = """
            query userCalendar($username: String!) {
                matchedUser(username: $username) {
                    submissionCalendar
                }
            }
        """;
        Map<String, Object> variables = Map.of("username", username);
        JsonNode response = executeGraphQLQuery(query, variables);
        String calendarStr = response.get("data").get("matchedUser").get("submissionCalendar").asText();
        return mapper.readTree(calendarStr);
    }

    private JsonNode executeGraphQLQuery(String query, Map<String, Object> variables) throws IOException, InterruptedException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("query", query);
        requestBody.put("variables", variables);

        String jsonBody = mapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GRAPHQL_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return mapper.readTree(response.body());
        } else {
            System.err.println("GraphQL request failed: " + response.body());
            throw new IOException("GraphQL request failed with status code: " + response.statusCode() + "\nResponse: " + response.body());
        }
    }
}