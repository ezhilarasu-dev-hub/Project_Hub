package com.projecthub.service;

import com.projecthub.dto.FacultyStatsDto;
import com.projecthub.dto.ProjectResponseDto;
import com.projecthub.dto.ProjectStatusUpdateRequest;
import com.projecthub.entity.Project;
import com.projecthub.entity.ProjectStatus;
import com.projecthub.entity.User;
import com.projecthub.exception.ResourceNotFoundException;
import com.projecthub.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FacultyService {

    private final ProjectRepository projectRepository;
    private final ProjectStatisticsService statisticsService;

    public FacultyService(ProjectRepository projectRepository, ProjectStatisticsService statisticsService) {
        this.projectRepository = projectRepository;
        this.statisticsService = statisticsService;
    }

    public FacultyStatsDto getStatistics() {
        return statisticsService.getDashboardStats();
    }

    public List<ProjectResponseDto> getAllProjects(User faculty, String search, ProjectStatus status) {
        List<Project> projects = projectRepository.findAllByOrderByCreatedAtDesc();

        return projects.stream()
                .filter(p -> {
                    if (status != null && p.getStatus() != status) {
                        return false;
                    }
                    if (StringUtils.hasText(search)) {
                        String query = search.toLowerCase().trim();
                        boolean titleMatch = p.getTitle().toLowerCase().contains(query);
                        boolean descMatch = p.getDescription().toLowerCase().contains(query);
                        boolean studentMatch = p.getStudent().getName().toLowerCase().contains(query) ||
                                              p.getStudent().getEmail().toLowerCase().contains(query);
                        return titleMatch || descMatch || studentMatch;
                    }
                    return true;
                })
                .map(p -> ProjectResponseDto.fromEntity(p, faculty))
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectResponseDto updateProjectStatus(Long projectId, ProjectStatusUpdateRequest request, User faculty) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        project.setStatus(request.getStatus());
        Project saved = projectRepository.save(project);
        return ProjectResponseDto.fromEntity(saved, faculty);
    }

    public ProjectResponseDto getProjectDetails(Long projectId, User faculty) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));
        return ProjectResponseDto.fromEntity(project, faculty);
    }
}
