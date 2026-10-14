package com.projecthub.service;

import com.projecthub.dto.FacultyStatsDto;
import com.projecthub.entity.ProjectStatus;
import com.projecthub.entity.ProjectVisibility;
import com.projecthub.entity.Role;
import com.projecthub.repository.ProjectRepository;
import com.projecthub.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProjectStatisticsService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectStatisticsService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public FacultyStatsDto getDashboardStats() {
        long totalProjects = projectRepository.count();
        long totalStudents = userRepository.countByRole(Role.STUDENT);
        long submitted = projectRepository.countByStatus(ProjectStatus.SUBMITTED);
        long approved = projectRepository.countByStatus(ProjectStatus.APPROVED);
        long rejected = projectRepository.countByStatus(ProjectStatus.REJECTED);

        return new FacultyStatsDto(totalProjects, totalStudents, submitted, approved, rejected);
    }

    public long getTotalProjectsCount() {
        return projectRepository.count();
    }

    public long getTotalStudentsCount() {
        return userRepository.countByRole(Role.STUDENT);
    }

    public long getApprovedProjectsCount() {
        return projectRepository.countByStatus(ProjectStatus.APPROVED);
    }

    public long getSubmittedProjectsCount() {
        return projectRepository.countByStatus(ProjectStatus.SUBMITTED);
    }

    public long getRejectedProjectsCount() {
        return projectRepository.countByStatus(ProjectStatus.REJECTED);
    }

    public long getFacultyOnlyProjectsCount() {
        return projectRepository.countBySourceCodeVisibility(ProjectVisibility.FACULTY_ONLY);
    }

    public long getProjectsSubmittedThisMonth() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        return projectRepository.countProjectsCreatedAfter(startOfMonth);
    }

    public Map<String, Object> getAllStatisticsAsMap() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalProjects", getTotalProjectsCount());
        stats.put("totalStudents", getTotalStudentsCount());
        stats.put("submittedProjects", getSubmittedProjectsCount());
        stats.put("approvedProjects", getApprovedProjectsCount());
        stats.put("rejectedProjects", getRejectedProjectsCount());
        stats.put("projectsThisMonth", getProjectsSubmittedThisMonth());
        stats.put("facultyOnlyProjects", getFacultyOnlyProjectsCount());
        return stats;
    }
}
