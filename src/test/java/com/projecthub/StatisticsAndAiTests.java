package com.projecthub;

import com.projecthub.ai.orchestration.AIOrchestrator;
import com.projecthub.dto.AiChatResponse;
import com.projecthub.entity.User;
import com.projecthub.repository.KnowledgeDocumentRepository;
import com.projecthub.repository.UserRepository;
import com.projecthub.service.ProjectStatisticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class StatisticsAndAiTests {

    @Autowired
    private ProjectStatisticsService statisticsService;

    @Autowired
    private AIOrchestrator aiOrchestrator;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KnowledgeDocumentRepository knowledgeRepository;

    @Test
    void testExactDatabaseStatisticsCounts() {
        long totalProjects = statisticsService.getTotalProjectsCount();
        long totalStudents = statisticsService.getTotalStudentsCount();

        assertTrue(totalProjects >= 3, "Database initializer should have created at least 3 initial projects");
        assertTrue(totalStudents >= 2, "Database initializer should have created at least 2 students");
    }

    @Test
    void testAiOrchestratorRoutesStatisticsQuery() {
        User student = userRepository.findByEmail("student@institution.edu").orElseThrow();

        AiChatResponse response = aiOrchestrator.process("How many projects have been uploaded?", student);

        assertNotNull(response);
        assertEquals("STATISTICS", response.getRequestType());
        assertNotNull(response.getStatistics());
        assertTrue(response.getReply().toLowerCase().contains("project"));
    }

    @Test
    void testAiOrchestratorGroundedWhenNoSimilarProjectExists() {
        // Clear knowledge documents to guarantee no similar project exists
        knowledgeRepository.deleteAll();

        User student = userRepository.findByEmail("student@institution.edu").orElseThrow();

        AiChatResponse response = aiOrchestrator.process("Has an underwater submarine laser communication system already been done by seniors?", student);

        assertNotNull(response);
        assertEquals("RAG_SEMANTIC", response.getRequestType());
        assertTrue(response.getReply().contains("I couldn't find a sufficiently similar project in the current senior-project database."),
                "When no project is found, AI must clearly state so without inventing projects.");
        assertFalse(response.getReply().contains("No one in the college has ever done this"),
                "AI must never say 'No one in the college has ever done this'.");
    }
}
