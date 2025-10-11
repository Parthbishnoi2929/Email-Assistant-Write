package com.email.Email_Writer.service;

import com.email.Email_Writer.Controller.EmailRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class EmailService {

    private final WebClient webclient;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    public EmailService(WebClient.Builder webclientBuilder) {
        this.webclient = webclientBuilder.build();
    }

    public String EmailReply(EmailRequest emailRequest) {
        // ✅ Build the prompt
        String prompt = buildprompt(emailRequest);

        // ✅ Create request body
        Map<String, Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        // ✅ Send the request
        String response = webclient.post()
                .uri(geminiApiUrl + "?key=" + geminiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just("{\"error\":\"" + e.getMessage() + "\"}");
                })
                .block();

        // ✅ Extract the AI-generated text
        return extractResponseContent(response);
    }

    // ✅ Use this new buildprompt method
    private String buildprompt(EmailRequest emailRequest) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a professional email reply based on the content below.");
        if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
            prompt.append(" Use a ").append(emailRequest.getTone()).append(" tone.");
        }
        prompt.append("\n\nEmail Content:\n").append(emailRequest.getContent());
        return prompt.toString();
    }

    private String extractResponseContent(String response) {
        try {
            if (response == null || response.isBlank()) {
                return "Empty response from model";
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            // If error node exists
            JsonNode errorNode = root.path("error");
            if (!errorNode.isMissingNode()) {
                String message = errorNode.path("message").asText("Unknown error");
                return "Model error: " + message;
            }

            JsonNode candidates = root.path("candidates");
            if (candidates.isMissingNode() || !candidates.isArray() || candidates.size() == 0) {
                return "No candidates returned by model";
            }

            JsonNode first = candidates.get(0);
            JsonNode textNode = first.path("content").path("parts");
            if (!textNode.isArray() || textNode.size() == 0) {
                return "No content in model response";
            }

            return textNode.get(0).path("text").asText("");
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }
}
