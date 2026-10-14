package com.projecthub.dto;

import com.projecthub.entity.Project;
import com.projecthub.entity.ProjectStatus;
import com.projecthub.entity.ProjectVisibility;
import com.projecthub.entity.Role;
import com.projecthub.entity.User;

import java.time.LocalDateTime;

public class ProjectResponseDto {

    private Long id;
    private String title;
    private String description;
    private Long studentId;
    private String studentName;
    private String studentEmail;
    private boolean hasSourceCode;
    private String sourceCodeFileName;
    private Long sourceCodeFileSize;
    private LocalDateTime sourceCodeUploadTimestamp;
    private boolean canDownloadSourceCode;
    private String deployedUrl;
    private boolean canViewDeployedUrl;
    private ProjectVisibility sourceCodeVisibility;
    private ProjectVisibility deployedLinkVisibility;
    private ProjectStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isOwner;

    public ProjectResponseDto() {
    }

    public static ProjectResponseDto fromEntity(Project project, User currentUser) {
        ProjectResponseDto dto = new ProjectResponseDto();
        dto.setId(project.getId());
        dto.setTitle(project.getTitle());
        dto.setDescription(project.getDescription());
        dto.setStudentId(project.getStudent().getId());
        dto.setStudentName(project.getStudent().getName());
        dto.setStudentEmail(project.getStudent().getEmail());
        dto.setSourceCodeVisibility(project.getSourceCodeVisibility());
        dto.setDeployedLinkVisibility(project.getDeployedLinkVisibility());
        dto.setStatus(project.getStatus());
        dto.setCreatedAt(project.getCreatedAt());
        dto.setUpdatedAt(project.getUpdatedAt());

        boolean isFaculty = currentUser != null && currentUser.getRole() == Role.FACULTY;
        boolean isOwner = currentUser != null && currentUser.getId().equals(project.getStudent().getId());
        dto.setOwner(isOwner);

        boolean hasFile = project.getSourceCodePath() != null && !project.getSourceCodePath().isEmpty();
        dto.setHasSourceCode(hasFile);

        // Source Code Permission Enforcement
        if (hasFile) {
            boolean allowedToDownload = isFaculty || isOwner || project.getSourceCodeVisibility() == ProjectVisibility.ALL_STUDENTS;
            dto.setCanDownloadSourceCode(allowedToDownload);
            if (allowedToDownload) {
                dto.setSourceCodeFileName(project.getSourceCodeFileName());
                dto.setSourceCodeFileSize(project.getSourceCodeFileSize());
                dto.setSourceCodeUploadTimestamp(project.getSourceCodeUploadTimestamp());
            } else {
                dto.setSourceCodeFileName(null);
                dto.setSourceCodeFileSize(null);
                dto.setSourceCodeUploadTimestamp(null);
            }
        } else {
            dto.setCanDownloadSourceCode(false);
        }

        // Deployed Link Permission Enforcement
        boolean allowedToViewLink = isFaculty || isOwner || project.getDeployedLinkVisibility() == ProjectVisibility.ALL_STUDENTS;
        dto.setCanViewDeployedUrl(allowedToViewLink);
        if (allowedToViewLink) {
            dto.setDeployedUrl(project.getDeployedUrl());
        } else {
            dto.setDeployedUrl(null);
        }

        return dto;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public boolean isHasSourceCode() {
        return hasSourceCode;
    }

    public void setHasSourceCode(boolean hasSourceCode) {
        this.hasSourceCode = hasSourceCode;
    }

    public String getSourceCodeFileName() {
        return sourceCodeFileName;
    }

    public void setSourceCodeFileName(String sourceCodeFileName) {
        this.sourceCodeFileName = sourceCodeFileName;
    }

    public Long getSourceCodeFileSize() {
        return sourceCodeFileSize;
    }

    public void setSourceCodeFileSize(Long sourceCodeFileSize) {
        this.sourceCodeFileSize = sourceCodeFileSize;
    }

    public LocalDateTime getSourceCodeUploadTimestamp() {
        return sourceCodeUploadTimestamp;
    }

    public void setSourceCodeUploadTimestamp(LocalDateTime sourceCodeUploadTimestamp) {
        this.sourceCodeUploadTimestamp = sourceCodeUploadTimestamp;
    }

    public boolean isCanDownloadSourceCode() {
        return canDownloadSourceCode;
    }

    public void setCanDownloadSourceCode(boolean canDownloadSourceCode) {
        this.canDownloadSourceCode = canDownloadSourceCode;
    }

    public String getDeployedUrl() {
        return deployedUrl;
    }

    public void setDeployedUrl(String deployedUrl) {
        this.deployedUrl = deployedUrl;
    }

    public boolean isCanViewDeployedUrl() {
        return canViewDeployedUrl;
    }

    public void setCanViewDeployedUrl(boolean canViewDeployedUrl) {
        this.canViewDeployedUrl = canViewDeployedUrl;
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

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }
}
