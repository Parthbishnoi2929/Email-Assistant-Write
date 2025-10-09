package com.email.Email_Writer.Controller;

import com.email.Email_Writer.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*")
public class EmailController {


    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateEmail(@RequestBody EmailRequest emailRequest) {
        String response = emailService.EmailReply(emailRequest);

        // return clean JSON instead of plain string
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "generatedEmail", response
        ));
    }
}
