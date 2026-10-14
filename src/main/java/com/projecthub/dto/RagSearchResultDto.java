package com.projecthub.dto;

public class RagSearchResultDto {

    private Long id;
    private String title;
    private String batch;
    private String authors;
    private String description;
    private String technologies;
    private double similarityScore;

    public RagSearchResultDto() {
    }

    public RagSearchResultDto(Long id, String title, String batch, String authors, String description, String technologies, double similarityScore) {
        this.id = id;
        this.title = title;
        this.batch = batch;
        this.authors = authors;
        this.description = description;
        this.technologies = technologies;
        this.similarityScore = similarityScore;
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

    public double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }
}
