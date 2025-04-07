package org.example.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.util.responses.SolvedProblemsResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class LeetCodeUtil {
    private static final String BASE_URL = "https://alfa-leetcode-api.onrender.com";
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Get solved problems statistics for a specific user as a JsonNode
     * @param username The LeetCode username
     * @return JsonNode with the response
     * @throws IOException If an I/O error occurs
     */
    public JsonNode getUserSolvedProblemsAsJson(String username) throws IOException {
        String response = sendGetRequest(BASE_URL + "/" + username + "/solved");
        return objectMapper.readTree(response);
    }

    /**
     * Get last 20 submissions for a specific user as a JsonNode
     * @param username The LeetCode username
     * @return JsonNode with the response
     * @throws IOException If an I/O error occurs
     */
    public JsonNode getUserSubmissionsAsJson(String username) throws IOException {
        String response = sendGetRequest(BASE_URL + "/" + username + "/submission");
        return objectMapper.readTree(response);
    }

    /**
     * Get submission calendar for a specific user as a JsonNode
     * @param username The LeetCode username
     * @return JsonNode with the response
     * @throws IOException If an I/O error occurs
     */
    public JsonNode getUserCalendarAsJson(String username) throws IOException {
        String response = sendGetRequest(BASE_URL + "/" + username + "/calendar");
        JsonNode rawNode = objectMapper.readTree(response);
        // The calendar comes as a JSON string inside the JSON response, so we need to parse it again
        String calendarStr = rawNode.get("submissionCalendar").asText();
        return objectMapper.readTree(calendarStr);
    }

    /**
     * Get problems with filters as a JsonNode
     * @param limit Maximum number of problems to return
     * @param tags Tags to filter by (comma separated)
     * @param skip Number of problems to skip
     * @param difficulty Difficulty level (EASY, MEDIUM, HARD)
     * @return JsonNode with the response
     * @throws IOException If an I/O error occurs
     */
    public JsonNode getProblemsAsJson(Integer limit, String tags, Integer skip, String difficulty) throws IOException {
        Map<String, String> params = new HashMap<>();
        if (limit != null) {
            params.put("limit", String.valueOf(limit));
        }
        params.put("tags", tags);
        if (skip != null) {
            params.put("skip", String.valueOf(skip));
        }
        if (difficulty != null) {
            params.put("difficulty", difficulty);
        }

        String response = sendGetRequest(BASE_URL + "/problems" + buildQueryString(params));
        return objectMapper.readTree(response);
    }

    // Same helper methods as before
    private String sendGetRequest(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } else {
            throw new IOException("GET request failed with response code: " + responseCode);
        }
    }

    private String buildQueryString(Map<String, String> params) {
        if (params.isEmpty()) {
            return "";
        }

        StringBuilder queryString = new StringBuilder("?");
        boolean first = true;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                queryString.append("&");
            }
            queryString.append(entry.getKey()).append("=").append(entry.getValue().replace(" ", "+"));
            first = false;
        }

        return queryString.toString();
    }
}
