package com.projecthub.dto;

import com.projecthub.entity.ProjectVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProjectUpdateRequest {

    @NotBlank(message = "Project title is required")
    @Size(min = 3, max = 255, message = "Project title must be between 3 and 255 characters")
    private String title;

    @NotBlank(message = "Project description is required")
    @Size(min = 10, max = 4000, message = "Project description must be between 10 and 4000 characters")
    private String description;

    private String deployedUrl;

    @NotNull(message = "Source code visibility is required")
    private ProjectVisibility sourceCodeVisibility;

    @NotNull(message = "Deployed link visibility is required")
    private ProjectVisibility deployedLinkVisibility;

    public ProjectUpdateRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeployedUrl() {
        return deployedUrl;
    }

    public void setDeployedUrl(String deployedUrl) {
        this.deployedUrl = deployedUrl;
    }

    public ProjectVisibility getSourceCodeVisibility() {
        return sourceCodeVisibility;
    }

    public void setSourceCodeVisibility(ProjectVisibility sourceCodeVisibility) {
        this.sourceCodeVisibility = sourceCodeVisibility;
    }

    public ProjectVisibility getDeployedLinkVisibility() {
        return deployedLinkVisibility;
    }

    public void setDeployedLinkVisibility(ProjectVisibility deployedLinkVisibility) {
        this.deployedLinkVisibility = deployedLinkVisibility;
    }
}
