package com.projecthub;

import com.projecthub.dto.ProjectCreateRequest;
import com.projecthub.dto.ProjectResponseDto;
import com.projecthub.dto.ProjectUpdateRequest;
import com.projecthub.entity.ProjectStatus;
import com.projecthub.entity.ProjectVisibility;
import com.projecthub.entity.User;
import com.projecthub.exception.ForbiddenException;
import com.projecthub.repository.UserRepository;
import com.projecthub.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ProjectSecurityTests {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testStudentProjectCreationAndOwnership() {
        User student = userRepository.findByEmail("student@institution.edu").orElseThrow();

        ProjectCreateRequest req = new ProjectCreateRequest();
        req.setTitle("AI Campus Navigation Bot");
        req.setDescription("Interactive mapping bot for newly enrolled university students.");
        req.setSourceCodeVisibility(ProjectVisibility.FACULTY_ONLY);
        req.setDeployedLinkVisibility(ProjectVisibility.ALL_STUDENTS);
        req.setDeployedUrl("https://nav.institution.edu");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "navbot.zip",
                "application/zip",
                "sample zip content".getBytes()
        );

        ProjectResponseDto created = projectService.createProject(req, file, student);

        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("AI Campus Navigation Bot", created.getTitle());
        assertEquals(ProjectStatus.SUBMITTED, created.getStatus());
        assertTrue(created.isOwner());
        assertTrue(created.isHasSourceCode());
    }

    @Test
    void testStudentCannotEditAnotherStudentsProject() {
        User student1 = userRepository.findByEmail("student@institution.edu").orElseThrow();
        User student2 = userRepository.findByEmail("student2@institution.edu").orElseThrow();

        // Student 1 creates project
        ProjectCreateRequest req = new ProjectCreateRequest();
        req.setTitle("Student 1 Confidential Project");
        req.setDescription("Important research project by student 1.");
        req.setSourceCodeVisibility(ProjectVisibility.FACULTY_ONLY);
        req.setDeployedLinkVisibility(ProjectVisibility.ALL_STUDENTS);

        ProjectResponseDto created = projectService.createProject(req, null, student1);

        // Student 2 attempts to edit Student 1's project
        ProjectUpdateRequest updateReq = new ProjectUpdateRequest();
        updateReq.setTitle("Malicious Overwrite");
        updateReq.setDescription("Attempted unauthorized modification.");
        updateReq.setSourceCodeVisibility(ProjectVisibility.ALL_STUDENTS);
        updateReq.setDeployedLinkVisibility(ProjectVisibility.ALL_STUDENTS);

        assertThrows(ForbiddenException.class, () -> {
            projectService.updateProject(created.getId(), updateReq, null, student2);
        });
    }

    @Test
    void testStudentCannotDownloadFacultyOnlySourceCodeOfAnotherStudent() {
        User student1 = userRepository.findByEmail("student@institution.edu").orElseThrow();
        User student2 = userRepository.findByEmail("student2@institution.edu").orElseThrow();

        ProjectCreateRequest req = new ProjectCreateRequest();
        req.setTitle("Restricted Hardware Architecture");
        req.setDescription("Proprietary embedded systems code.");
        req.setSourceCodeVisibility(ProjectVisibility.FACULTY_ONLY);
        req.setDeployedLinkVisibility(ProjectVisibility.FACULTY_ONLY);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "embedded.zip",
                "application/zip",
                "proprietary hardware design".getBytes()
        );

        ProjectResponseDto created = projectService.createProject(req, file, student1);

        // Student 2 tries to download student 1's faculty-only code
        assertThrows(ForbiddenException.class, () -> {
            projectService.downloadSourceCode(created.getId(), student2);
        });

        // But Student 1 (owner) CAN download
        assertNotNull(projectService.downloadSourceCode(created.getId(), student1));

        // And Faculty CAN download
        User faculty = userRepository.findByEmail("faculty@institution.edu").orElseThrow();
        assertNotNull(projectService.downloadSourceCode(created.getId(), faculty));
    }
}
