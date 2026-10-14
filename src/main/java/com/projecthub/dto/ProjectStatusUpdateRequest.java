package com.projecthub.dto;

import com.projecthub.entity.ProjectStatus;
import jakarta.validation.constraints.NotNull;

public class ProjectStatusUpdateRequest {

    @NotNull(message = "Status is required (SUBMITTED, APPROVED, or REJECTED)")
    private ProjectStatus status;

    public ProjectStatusUpdateRequest() {
    }

    public ProjectStatusUpdateRequest(ProjectStatus status) {
        this.status = status;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }
}
