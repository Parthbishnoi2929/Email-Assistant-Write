package com.email.Email_Writer.Controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class EmailRequest {
    private String content;
    private String tone;

    public EmailRequest(String content, String tone) {
        this.content = content;
        this.tone = tone;
    }
    public EmailRequest() {
        this.content = content;
        this.tone = tone;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }
}
