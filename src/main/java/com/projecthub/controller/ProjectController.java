package com.projecthub.controller;

import com.projecthub.dto.ProjectCreateRequest;
import com.projecthub.dto.ProjectResponseDto;
import com.projecthub.dto.ProjectUpdateRequest;
import com.projecthub.entity.Project;
import com.projecthub.entity.ProjectVisibility;
import com.projecthub.entity.User;
import com.projecthub.service.ProjectService;
import com.projecthub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    public ProjectController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService = userService;
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ProjectResponseDto> createProject(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "deployedUrl", required = false) String deployedUrl,
            @RequestParam(value = "sourceCodeVisibility", defaultValue = "FACULTY_ONLY") ProjectVisibility sourceCodeVisibility,
            @RequestParam(value = "deployedLinkVisibility", defaultValue = "ALL_STUDENTS") ProjectVisibility deployedLinkVisibility,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication authentication) {

        User student = userService.getByEmail(authentication.getName());

        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setDeployedUrl(deployedUrl);
        request.setSourceCodeVisibility(sourceCodeVisibility);
        request.setDeployedLinkVisibility(deployedLinkVisibility);

        ProjectResponseDto created = projectService.createProject(request, file, student);
        return ResponseEntity.ok(created);
    }

    @PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ProjectResponseDto> updateProject(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "deployedUrl", required = false) String deployedUrl,
            @RequestParam(value = "sourceCodeVisibility") ProjectVisibility sourceCodeVisibility,
            @RequestParam(value = "deployedLinkVisibility") ProjectVisibility deployedLinkVisibility,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication authentication) {

        User student = userService.getByEmail(authentication.getName());

        ProjectUpdateRequest request = new ProjectUpdateRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setDeployedUrl(deployedUrl);
        request.setSourceCodeVisibility(sourceCodeVisibility);
        request.setDeployedLinkVisibility(deployedLinkVisibility);

        ProjectResponseDto updated = projectService.updateProject(id, request, file, student);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ProjectResponseDto>> getMyProjects(Authentication authentication) {
        User student = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(projectService.getMyProjects(student));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponseDto>> getAllAccessibleProjects(Authentication authentication) {
        User currentUser = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(projectService.getAccessibleProjects(currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDto> getProjectById(@PathVariable Long id, Authentication authentication) {
        User currentUser = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(projectService.getProjectById(id, currentUser));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadSourceCode(@PathVariable Long id, Authentication authentication) {
        User currentUser = userService.getByEmail(authentication.getName());
        Project project = projectService.getEntityById(id);
        Resource resource = projectService.downloadSourceCode(id, currentUser);

        String filename = project.getSourceCodeFileName() != null ? project.getSourceCodeFileName() : "source_code.zip";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}
