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

    @Value("${openrouter.api.url}")
    private String openRouterUrl;

    @Value("${openrouter.api.key}")
    private String openRouterKey;

    public EmailService(WebClient.Builder webclientBuilder) {
        this.webclient = webclientBuilder.build();
    }

    public String EmailReply(EmailRequest emailRequest) {
        String prompt = buildPrompt(emailRequest);

        Map<String, Object> requestBody = Map.of(
                "model", "mistralai/mistral-7b-instruct:free", // ✅ Free model
                "messages", new Object[]{
                        Map.of("role", "system", "content", "You are a helpful email assistant."),
                        Map.of("role", "user", "content", prompt)
                },
                "temperature", 0.7
        );

        String response = webclient.post()
                .uri(openRouterUrl)
                .header("Authorization", "Bearer " + openRouterKey)
                .header("HTTP-Referer", "https://your-app-name.com") // optional but recommended
                .header("X-Title", "Email Writer App")               // shows in OpenRouter dashboard
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just("{\"error\":\"" + e.getMessage() + "\"}");
                })
                .block();

        return extractResponse(response);
    }

    private String extractResponse(String response) {
        try {
            if (response == null || response.isBlank()) {
                return "Empty response from model.";
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            JsonNode errorNode = root.path("error");
            if (!errorNode.isMissingNode()) {
                return "API Error: " + errorNode.toString();
            }

            JsonNode choices = root.path("choices");
            if (!choices.isArray() || choices.size() == 0) {
                return "No response generated.";
            }

            return choices.get(0).path("message").path("content").asText("No content.");
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder sb = new StringBuilder();
        sb.append("Write a professional email reply for the following message.");
        if (emailRequest.getTone() != null && !emailRequest.getTone().isBlank()) {
            sb.append(" Use a ").append(emailRequest.getTone()).append(" tone.");
        }
        sb.append("\n\nOriginal Email:\n").append(emailRequest.getContent());
        return sb.toString();
    }
}
