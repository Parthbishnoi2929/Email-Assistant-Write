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

    private final WebClient webClient;

    @Value("${hf.api.url}")
    private String hfApiUrl;

    public EmailService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String EmailReply(EmailRequest emailRequest) {
        String prompt = buildPrompt(emailRequest);

        Map<String, Object> requestBody = Map.of("inputs", prompt);

        String response = webClient.post()
                .uri(hfApiUrl)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just("{\"error\":\"" + e.getMessage() + "\"}");
                })
                .block();

        return extractResponseContent(response);
    }

    private String extractResponseContent(String response) {
        try {
            if (response == null || response.isBlank()) return "Empty response from model";

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            // For Hugging Face, result is usually an array with "generated_text"
            if (root.isArray() && root.size() > 0) {
                return root.get(0).path("generated_text").asText("No output");
            }
            return "Unexpected response: " + response;
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage();
        }
    }

    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a professional email reply for the following content.");
        if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
            prompt.append(" Use a ").append(emailRequest.getTone()).append(" tone.");
        }
        prompt.append("\nEmail:\n").append(emailRequest.getContent());
        return prompt.toString();
    }
}
