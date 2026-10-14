package com.projecthub.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "source_code_file_name")
    private String sourceCodeFileName;

    @Column(name = "source_code_path")
    private String sourceCodePath;

    @Column(name = "source_code_file_size")
    private Long sourceCodeFileSize;

    @Column(name = "source_code_upload_timestamp")
    private LocalDateTime sourceCodeUploadTimestamp;

    @Column(name = "deployed_url")
    private String deployedUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_code_visibility", nullable = false)
    private ProjectVisibility sourceCodeVisibility = ProjectVisibility.FACULTY_ONLY;

    @Enumerated(EnumType.STRING)
    @Column(name = "deployed_link_visibility", nullable = false)
    private ProjectVisibility deployedLinkVisibility = ProjectVisibility.ALL_STUDENTS;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status = ProjectStatus.SUBMITTED;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Project() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public String getSourceCodeFileName() {
        return sourceCodeFileName;
    }

    public void setSourceCodeFileName(String sourceCodeFileName) {
        this.sourceCodeFileName = sourceCodeFileName;
    }

    public String getSourceCodePath() {
        return sourceCodePath;
    }

    public void setSourceCodePath(String sourceCodePath) {
        this.sourceCodePath = sourceCodePath;
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
}
