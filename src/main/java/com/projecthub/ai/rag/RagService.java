package com.projecthub.ai.rag;

import com.projecthub.ai.embedding.EmbeddingService;
import com.projecthub.dto.RagReindexResponse;
import com.projecthub.dto.RagSearchResultDto;
import com.projecthub.entity.KnowledgeDocument;
import com.projecthub.exception.BadRequestException;
import com.projecthub.repository.KnowledgeDocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final KnowledgeDocumentRepository knowledgeRepository;
    private final SeniorProjectsParser parser;
    private final EmbeddingService embeddingService;
    private final String knowledgeFilePath;

    public RagService(KnowledgeDocumentRepository knowledgeRepository,
                      SeniorProjectsParser parser,
                      EmbeddingService embeddingService,
                      @Value("${app.rag.knowledge-file:senior_projects.txt}") String knowledgeFilePath) {
        this.knowledgeRepository = knowledgeRepository;
        this.parser = parser;
        this.embeddingService = embeddingService;
        this.knowledgeFilePath = knowledgeFilePath;
    }

    @Transactional
    public RagReindexResponse reindexKnowledgeBase() {
        Path path = Paths.get(knowledgeFilePath).toAbsolutePath().normalize();

        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                throw new BadRequestException("Could not create knowledge base file: " + knowledgeFilePath);
            }
        }

        String content;
        try {
            content = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BadRequestException("Failed to read senior_projects.txt: " + e.getMessage());
        }

        List<KnowledgeDocument> parsedDocuments = parser.parse(content);

        // Clear existing knowledge base to prevent duplicate entries
        knowledgeRepository.deleteAll();

        if (parsedDocuments.isEmpty()) {
            return new RagReindexResponse(0, "SUCCESS", "Knowledge base file is currently empty. 0 documents indexed.");
        }

        // Generate vector embeddings and store in SQLite
        for (KnowledgeDocument doc : parsedDocuments) {
            float[] embedding = embeddingService.generateEmbedding(doc.getNormalizedContent());
            doc.setEmbeddingJson(embeddingService.serializeVector(embedding));
            knowledgeRepository.save(doc);
        }

        return new RagReindexResponse(parsedDocuments.size(), "SUCCESS", "Successfully processed and indexed " + parsedDocuments.size() + " senior project documents.");
    }

    public List<RagSearchResultDto> searchSimilar(String query, int topK) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<KnowledgeDocument> allDocuments = knowledgeRepository.findAll();
        if (allDocuments.isEmpty()) {
            // Attempt auto-reindex if records exist in file but DB is empty
            Path path = Paths.get(knowledgeFilePath).toAbsolutePath().normalize();
            if (Files.exists(path)) {
                reindexKnowledgeBase();
                allDocuments = knowledgeRepository.findAll();
            }
        }

        if (allDocuments.isEmpty()) {
            return new ArrayList<>();
        }

        float[] queryEmbedding = embeddingService.generateEmbedding(query.trim());

        List<RagSearchResultDto> scoredResults = new ArrayList<>();
        for (KnowledgeDocument doc : allDocuments) {
            float[] docEmbedding = embeddingService.deserializeVector(doc.getEmbeddingJson());
            double score = embeddingService.cosineSimilarity(queryEmbedding, docEmbedding);

            scoredResults.add(new RagSearchResultDto(
                    doc.getId(),
                    doc.getTitle(),
                    doc.getBatch(),
                    doc.getAuthors(),
                    doc.getDescription(),
                    doc.getTechnologies(),
                    score
            ));
        }

        // Sort descending by similarity score and take top K
        return scoredResults.stream()
                .sorted(Comparator.comparingDouble(RagSearchResultDto::getSimilarityScore).reversed())
                .limit(topK > 0 ? topK : 3)
                .collect(Collectors.toList());
    }

    public long getIndexedDocumentCount() {
        return knowledgeRepository.count();
    }
}
