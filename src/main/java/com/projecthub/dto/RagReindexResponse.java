package com.projecthub.dto;

public class RagReindexResponse {

    private int documentsProcessed;
    private String status;
    private String message;

    public RagReindexResponse() {
    }

    public RagReindexResponse(int documentsProcessed, String status, String message) {
        this.documentsProcessed = documentsProcessed;
        this.status = status;
        this.message = message;
    }

    public int getDocumentsProcessed() {
        return documentsProcessed;
    }

    public void setDocumentsProcessed(int documentsProcessed) {
        this.documentsProcessed = documentsProcessed;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
