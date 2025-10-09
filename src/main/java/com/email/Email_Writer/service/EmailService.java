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
        //build a prompt
        String prompt=buildprompt(emailRequest);

        //crafting request
        Map<String, Object> requstbody=Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );
        //do request
        String response=webclient.post()
                .uri(geminiApiUrl+ "?key=" +geminiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requstbody)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just("{\"error\":\"" + e.getMessage() + "\"}");
                })
                .block();
        //response
        return extractResponseContent(response);


    }

    private String extractResponseContent(String response) {
        try{
            if (response == null || response.isBlank()) {
                return "Empty response from model";
            }
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            // Return API error message if present
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

    private String buildprompt(EmailRequest emailRequest) {
        StringBuilder prompt=new StringBuilder();
        prompt.append("generate a professional email reply for email content");
         if(emailRequest.getTone()!=null && !emailRequest.getTone().isEmpty()) {
             prompt.append(" Use a").append(emailRequest.getTone()).append(" tone.");
         }
         prompt.append("\nEmail: \n").append(emailRequest.getContent());
         return prompt.toString();
    }
}
