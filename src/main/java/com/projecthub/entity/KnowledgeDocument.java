package com.projecthub.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "knowledge_documents")
public class KnowledgeDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String batch;

    private String authors;

    @Column(length = 4000)
    private String description;

    private String technologies;

    @Column(name = "normalized_content", length = 5000)
    private String normalizedContent;

    @Column(name = "embedding_json", length = 10000)
    private String embeddingJson;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public KnowledgeDocument() {
    }

    public KnowledgeDocument(String title, String batch, String authors, String description, String technologies) {
        this.title = title;
        this.batch = batch;
        this.authors = authors;
        this.description = description;
        this.technologies = technologies;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTechnologies() {
        return technologies;
    }

    public void setTechnologies(String technologies) {
        this.technologies = technologies;
    }

    public String getNormalizedContent() {
        return normalizedContent;
    }

    public void setNormalizedContent(String normalizedContent) {
        this.normalizedContent = normalizedContent;
    }

    public String getEmbeddingJson() {
        return embeddingJson;
    }

    public void setEmbeddingJson(String embeddingJson) {
        this.embeddingJson = embeddingJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
