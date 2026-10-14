package com.projecthub.dto;

import jakarta.validation.constraints.NotBlank;

public class RagSearchRequest {

    @NotBlank(message = "Search query is required")
    private String query;

    private int topK = 3;

    public RagSearchRequest() {
    }

    public RagSearchRequest(String query, int topK) {
        this.query = query;
        this.topK = topK > 0 ? topK : 3;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }
}
