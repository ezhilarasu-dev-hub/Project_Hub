package com.projecthub.dto;

import java.util.List;
import java.util.Map;

public class AiChatResponse {

    private String reply;
    private String requestType;
    private List<RagSearchResultDto> retrievedProjects;
    private Map<String, Object> statistics;
    private boolean groqPowered;

    public AiChatResponse() {
    }

    public AiChatResponse(String reply, String requestType, List<RagSearchResultDto> retrievedProjects, Map<String, Object> statistics, boolean groqPowered) {
        this.reply = reply;
        this.requestType = requestType;
        this.retrievedProjects = retrievedProjects;
        this.statistics = statistics;
        this.groqPowered = groqPowered;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public List<RagSearchResultDto> getRetrievedProjects() {
        return retrievedProjects;
    }

    public void setRetrievedProjects(List<RagSearchResultDto> retrievedProjects) {
        this.retrievedProjects = retrievedProjects;
    }

    public Map<String, Object> getStatistics() {
        return statistics;
    }

    public void setStatistics(Map<String, Object> statistics) {
        this.statistics = statistics;
    }

    public boolean isGroqPowered() {
        return groqPowered;
    }

    public void setGroqPowered(boolean groqPowered) {
        this.groqPowered = groqPowered;
    }
}
