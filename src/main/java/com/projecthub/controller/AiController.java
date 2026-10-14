package com.projecthub.controller;

import com.projecthub.ai.orchestration.AIOrchestrator;
import com.projecthub.dto.AiChatRequest;
import com.projecthub.dto.AiChatResponse;
import com.projecthub.entity.User;
import com.projecthub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AIOrchestrator aiOrchestrator;
    private final UserService userService;

    public AiController(AIOrchestrator aiOrchestrator, UserService userService) {
        this.aiOrchestrator = aiOrchestrator;
        this.userService = userService;
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request, Authentication authentication) {
        User currentUser = userService.getByEmail(authentication.getName());
        AiChatResponse response = aiOrchestrator.process(request.getMessage(), currentUser);
        return ResponseEntity.ok(response);
    }
}
