package org.example.util;

import java.io.IOException;

public class LeetCodeApiDemo {
    public static void main(String[] args) {
        LeetCodeUtil client = new LeetCodeUtil();
        String username = "artemka-web3"; // Example username from your provided data

        try {
            System.out.println("===== USER SOLVED PROBLEMS =====");
            String solvedProblems = String.valueOf(client.getUserSolvedProblemsAsJson(username));
            System.out.println(solvedProblems);
            System.out.println();

            System.out.println("===== USER SUBMISSIONS =====");
            String submissions = String.valueOf(client.getUserSubmissionsAsJson(username));
            System.out.println(submissions);
            System.out.println();

            System.out.println("===== USER CALENDAR =====");
            String calendar = String.valueOf(client.getUserCalendarAsJson(username));
            System.out.println(calendar);
            System.out.println();

            System.out.println("===== FILTERED PROBLEMS =====");
            String problems = String.valueOf(client.getProblemsAsJson(1, "binary-search", 20, "EASY"));
            System.out.println(problems);

        } catch (IOException e) {
            System.err.println("Error communicating with API: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
