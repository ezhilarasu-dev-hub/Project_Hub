package com.projecthub.controller;

import com.projecthub.ai.rag.RagService;
import com.projecthub.dto.RagReindexResponse;
import com.projecthub.dto.RagSearchRequest;
import com.projecthub.dto.RagSearchResultDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/reindex")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<RagReindexResponse> reindexKnowledgeBase() {
        RagReindexResponse response = ragService.reindexKnowledgeBase();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/search")
    public ResponseEntity<List<RagSearchResultDto>> searchSeniorProjects(@Valid @RequestBody RagSearchRequest request) {
        List<RagSearchResultDto> results = ragService.searchSimilar(request.getQuery(), request.getTopK());
        return ResponseEntity.ok(results);
    }
}
