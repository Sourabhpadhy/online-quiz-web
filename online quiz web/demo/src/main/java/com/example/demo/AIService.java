package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class AIService {

    private String apiKey = "AIzaSyCHzXTaz0dNRr9O5a9l1tLp_kJb2F4PSU0";

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    public List<Question> fetchQuestionsFromAI(String topic, String difficulty) {
        List<Question> questions = new ArrayList<>();
        
        try {
            String promptText = "Generate 10 " + difficulty + " multiple choice questions on " + topic + ". " +
                                "Return ONLY raw text data. No Markdown, no bolding, no intro. " +
                                "Format each line exactly like this: Question|OptionA|OptionB|OptionC|OptionD|CorrectLetter";

            String safePrompt = promptText.replace("\"", "\\\"");
            String jsonBody = "{ \"contents\": [{ \"parts\": [{ \"text\": \"" + safePrompt + "\" }] }] }";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_URL + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                questions = parseResponseRobust(response.body());
            } else {
                System.err.println("API Error: " + response.body());
                questions.add(new Question("API Error: " + response.statusCode(), "N/A", "N/A", "N/A", "N/A", "A"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            questions.add(new Question("Connection Failed", "N/A", "N/A", "N/A", "N/A", "A"));
        }

        if (questions.isEmpty()) {
            questions.add(new Question("No questions could be generated.", "N/A", "N/A", "N/A", "N/A", "A"));
        }
        return questions;
    }

    private List<Question> parseResponseRobust(String jsonResponse) {
        List<Question> list = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        
        try {
            JsonNode root = mapper.readTree(jsonResponse);
            String text = root.path("candidates").get(0)
                              .path("content").path("parts").get(0)
                              .path("text").asText();

            String[] lines = text.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                if (line.startsWith("|")) line = line.substring(1);
                if (line.endsWith("|")) line = line.substring(0, line.length() - 1);

                String[] parts = line.split("\\|");
                if (parts.length >= 6) {
                    list.add(new Question(
                        parts[0].trim(), 
                        parts[1].trim(), 
                        parts[2].trim(), 
                        parts[3].trim(), 
                        parts[4].trim(), 
                        parts[5].trim()
                    ));
                }
            }
        } catch (Exception e) {
            System.err.println("Parsing Error: " + e.getMessage());
        }
        return list;
    }
}