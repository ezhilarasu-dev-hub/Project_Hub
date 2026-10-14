package com.projecthub.repository;

import com.projecthub.entity.Project;
import com.projecthub.entity.ProjectStatus;
import com.projecthub.entity.ProjectVisibility;
import com.projecthub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByStudentOrderByCreatedAtDesc(User student);

    List<Project> findAllByOrderByCreatedAtDesc();

    List<Project> findByStatusOrderByCreatedAtDesc(ProjectStatus status);

    long countByStatus(ProjectStatus status);

    long countBySourceCodeVisibility(ProjectVisibility visibility);

    @Query("SELECT COUNT(p) FROM Project p WHERE p.createdAt >= :startDate")
    long countProjectsCreatedAfter(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(DISTINCT p.student.id) FROM Project p")
    long countDistinctStudentsWithProjects();

    @Query("SELECT p FROM Project p WHERE p.status = 'APPROVED' OR p.student = :user ORDER BY p.createdAt DESC")
    List<Project> findAccessibleProjectsForStudent(@Param("user") User user);
}
