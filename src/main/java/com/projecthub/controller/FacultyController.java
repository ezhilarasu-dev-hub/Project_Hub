package com.projecthub.controller;

import com.projecthub.dto.FacultyStatsDto;
import com.projecthub.dto.ProjectResponseDto;
import com.projecthub.dto.ProjectStatusUpdateRequest;
import com.projecthub.entity.ProjectStatus;
import com.projecthub.entity.User;
import com.projecthub.service.FacultyService;
import com.projecthub.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faculty")
@PreAuthorize("hasRole('FACULTY')")
public class FacultyController {

    private final FacultyService facultyService;
    private final UserService userService;

    public FacultyController(FacultyService facultyService, UserService userService) {
        this.facultyService = facultyService;
        this.userService = userService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<FacultyStatsDto> getStatistics() {
        return ResponseEntity.ok(facultyService.getStatistics());
    }

    @GetMapping("/projects")
    public ResponseEntity<List<ProjectResponseDto>> getAllProjects(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) ProjectStatus status,
            Authentication authentication) {
        User faculty = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(facultyService.getAllProjects(faculty, search, status));
    }

    @GetMapping("/projects/{id}")
    public ResponseEntity<ProjectResponseDto> getProjectDetails(@PathVariable Long id, Authentication authentication) {
        User faculty = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(facultyService.getProjectDetails(id, faculty));
    }

    @PutMapping("/projects/{id}/status")
    public ResponseEntity<ProjectResponseDto> updateProjectStatus(
            @PathVariable Long id,
            @Valid @RequestBody ProjectStatusUpdateRequest request,
            Authentication authentication) {
        User faculty = userService.getByEmail(authentication.getName());
        return ResponseEntity.ok(facultyService.updateProjectStatus(id, request, faculty));
    }
}
