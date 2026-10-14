package com.projecthub.service;

import com.projecthub.dto.ProjectCreateRequest;
import com.projecthub.dto.ProjectResponseDto;
import com.projecthub.dto.ProjectUpdateRequest;
import com.projecthub.entity.Project;
import com.projecthub.entity.ProjectStatus;
import com.projecthub.entity.ProjectVisibility;
import com.projecthub.entity.Role;
import com.projecthub.entity.User;
import com.projecthub.exception.BadRequestException;
import com.projecthub.exception.ForbiddenException;
import com.projecthub.exception.ResourceNotFoundException;
import com.projecthub.repository.ProjectRepository;
import com.projecthub.storage.StorageService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final StorageService storageService;

    public ProjectService(ProjectRepository projectRepository, StorageService storageService) {
        this.projectRepository = projectRepository;
        this.storageService = storageService;
    }

    private void validateUrl(String url) {
        if (StringUtils.hasText(url)) {
            String trimmed = url.trim();
            if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                throw new BadRequestException("Deployed URL must start with http:// or https://");
            }
            try {
                URI uri = new URI(trimmed);
                if (uri.getHost() == null) {
                    throw new BadRequestException("Invalid URL structure.");
                }
            } catch (URISyntaxException e) {
                throw new BadRequestException("Invalid deployed project URL: " + e.getMessage());
            }
        }
    }

    @Transactional
    public ProjectResponseDto createProject(ProjectCreateRequest request, MultipartFile file, User student) {
        validateUrl(request.getDeployedUrl());

        Project project = new Project();
        project.setTitle(request.getTitle().trim());
        project.setDescription(request.getDescription().trim());
        project.setStudent(student);
        project.setSourceCodeVisibility(request.getSourceCodeVisibility());
        project.setDeployedLinkVisibility(request.getDeployedLinkVisibility());
        project.setStatus(ProjectStatus.SUBMITTED);
        project.setDeployedUrl(StringUtils.hasText(request.getDeployedUrl()) ? request.getDeployedUrl().trim() : null);

        if (file != null && !file.isEmpty()) {
            String storedPath = storageService.storeFile(file, "project_" + student.getId());
            project.setSourceCodeFileName(file.getOriginalFilename());
            project.setSourceCodePath(storedPath);
            project.setSourceCodeFileSize(file.getSize());
            project.setSourceCodeUploadTimestamp(LocalDateTime.now());
        }

        Project saved = projectRepository.save(project);
        return ProjectResponseDto.fromEntity(saved, student);
    }

    @Transactional
    public ProjectResponseDto updateProject(Long projectId, ProjectUpdateRequest request, MultipartFile file, User student) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        // Students can only edit their own projects
        if (!project.getStudent().getId().equals(student.getId())) {
            throw new ForbiddenException("You are not authorized to edit this project.");
        }

        validateUrl(request.getDeployedUrl());

        project.setTitle(request.getTitle().trim());
        project.setDescription(request.getDescription().trim());
        project.setSourceCodeVisibility(request.getSourceCodeVisibility());
        project.setDeployedLinkVisibility(request.getDeployedLinkVisibility());
        project.setDeployedUrl(StringUtils.hasText(request.getDeployedUrl()) ? request.getDeployedUrl().trim() : null);

        if (file != null && !file.isEmpty()) {
            // Delete old file if present
            if (StringUtils.hasText(project.getSourceCodePath())) {
                storageService.deleteFile(project.getSourceCodePath());
            }
            String storedPath = storageService.storeFile(file, "project_" + student.getId());
            project.setSourceCodeFileName(file.getOriginalFilename());
            project.setSourceCodePath(storedPath);
            project.setSourceCodeFileSize(file.getSize());
            project.setSourceCodeUploadTimestamp(LocalDateTime.now());
        }

        Project saved = projectRepository.save(project);
        return ProjectResponseDto.fromEntity(saved, student);
    }

    public List<ProjectResponseDto> getMyProjects(User student) {
        return projectRepository.findByStudentOrderByCreatedAtDesc(student).stream()
                .map(p -> ProjectResponseDto.fromEntity(p, student))
                .collect(Collectors.toList());
    }

    public List<ProjectResponseDto> getAccessibleProjects(User currentUser) {
        if (currentUser.getRole() == Role.FACULTY) {
            return projectRepository.findAllByOrderByCreatedAtDesc().stream()
                    .map(p -> ProjectResponseDto.fromEntity(p, currentUser))
                    .collect(Collectors.toList());
        } else {
            return projectRepository.findAccessibleProjectsForStudent(currentUser).stream()
                    .map(p -> ProjectResponseDto.fromEntity(p, currentUser))
                    .collect(Collectors.toList());
        }
    }

    public ProjectResponseDto getProjectById(Long projectId, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        // Visibility check for students
        if (currentUser.getRole() == Role.STUDENT && !project.getStudent().getId().equals(currentUser.getId())) {
            if (project.getStatus() != ProjectStatus.APPROVED) {
                throw new ForbiddenException("This project is not currently public.");
            }
        }

        return ProjectResponseDto.fromEntity(project, currentUser);
    }

    public Resource downloadSourceCode(Long projectId, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        if (!StringUtils.hasText(project.getSourceCodePath())) {
            throw new ResourceNotFoundException("No source code file is attached to this project.");
        }

        boolean isFaculty = currentUser.getRole() == Role.FACULTY;
        boolean isOwner = project.getStudent().getId().equals(currentUser.getId());
        boolean isAllStudents = project.getSourceCodeVisibility() == ProjectVisibility.ALL_STUDENTS;

        if (!isFaculty && !isOwner && !isAllStudents) {
            throw new ForbiddenException("Access Denied: You are not authorized to download this source code (Faculty Only).");
        }

        return storageService.loadFileAsResource(project.getSourceCodePath());
    }

    public Project getEntityById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
    }
}
