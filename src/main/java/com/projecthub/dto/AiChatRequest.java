package com.projecthub.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

public class AiChatRequest {

    @NotBlank(message = "Message cannot be empty")
    private String message;

    public AiChatRequest() {
    }

    public AiChatRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
