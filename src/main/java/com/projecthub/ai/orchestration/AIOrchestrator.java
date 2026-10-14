package com.projecthub.ai.orchestration;

import com.projecthub.ai.groq.GroqService;
import com.projecthub.ai.rag.RagService;
import com.projecthub.dto.AiChatResponse;
import com.projecthub.dto.RagSearchResultDto;
import com.projecthub.entity.Role;
import com.projecthub.entity.User;
import com.projecthub.service.ProjectStatisticsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AIOrchestrator {

    private static final double SIMILARITY_THRESHOLD = 0.12;

    private final RagService ragService;
    private final ProjectStatisticsService statisticsService;
    private final GroqService groqService;

    private static final Pattern STATS_PATTERN = Pattern.compile(
            "\\b(how many|count|number of|total|statistics|stats|submitted this month|approved count|how much|faculty-only)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern RAG_PATTERN = Pattern.compile(
            "\\b(senior|similar|already|done|chatbot|developed|created|interview|system|healthcare|idea|project like|find project|related to|batch)\\b",
            Pattern.CASE_INSENSITIVE
    );

    public AIOrchestrator(RagService ragService,
                          ProjectStatisticsService statisticsService,
                          GroqService groqService) {
        this.ragService = ragService;
        this.statisticsService = statisticsService;
        this.groqService = groqService;
    }

    public AiChatResponse process(String userMessage, User currentUser) {
        String trimmedMessage = userMessage != null ? userMessage.trim() : "";
        if (trimmedMessage.isEmpty()) {
            return new AiChatResponse("Please enter a message or question.", "UNKNOWN", List.of(), Map.of(), false);
        }

        boolean isStats = STATS_PATTERN.matcher(trimmedMessage).find();
        boolean isRag = RAG_PATTERN.matcher(trimmedMessage).find();

        // If neither pattern matched explicitly, evaluate both to be safe and helpful
        if (!isStats && !isRag) {
            isRag = true;
        }

        String requestType;
        if (isStats && isRag) {
            requestType = "HYBRID";
        } else if (isStats) {
            requestType = "STATISTICS";
        } else {
            requestType = "RAG_SEMANTIC";
        }

        Map<String, Object> stats = null;
        if (isStats) {
            stats = statisticsService.getAllStatisticsAsMap();
            // If student, do not reveal faculty-only details in statistics
            if (currentUser.getRole() == Role.STUDENT) {
                stats.remove("facultyOnlyProjects");
            }
        }

        List<RagSearchResultDto> retrievedDocs = null;
        List<RagSearchResultDto> relevantDocs = new ArrayList<>();
        if (isRag) {
            retrievedDocs = ragService.searchSimilar(trimmedMessage, 3);
            for (RagSearchResultDto doc : retrievedDocs) {
                if (doc.getSimilarityScore() >= SIMILARITY_THRESHOLD) {
                    relevantDocs.add(doc);
                }
            }
        }

        // Build context for LLM
        String systemPrompt = buildSystemPrompt(currentUser);
        String contextPrompt = buildContextPrompt(trimmedMessage, stats, relevantDocs, isRag, isStats);

        String reply;
        boolean groqUsed = false;

        if (groqService.isApiKeyConfigured()) {
            String llmOutput = groqService.generateChatCompletion(systemPrompt, contextPrompt);
            if (StringUtils.hasText(llmOutput) && !llmOutput.startsWith("Unable to reach Groq")) {
                reply = llmOutput;
                groqUsed = true;
            } else {
                reply = buildFallbackResponse(trimmedMessage, stats, relevantDocs, isRag, isStats);
                if (StringUtils.hasText(llmOutput)) {
                    reply += "\n\n[" + llmOutput + "]";
                }
            }
        } else {
            // High quality local grounded fallback response
            reply = buildFallbackResponse(trimmedMessage, stats, relevantDocs, isRag, isStats);
        }

        return new AiChatResponse(reply, requestType, retrievedDocs, stats, groqUsed);
    }

    private String buildSystemPrompt(User currentUser) {
        return "You are the official Institution Project Hub AI Assistant for " +
                (currentUser.getRole() == Role.FACULTY ? "Faculty members." : "Students.") + "\n" +
                "CRITICAL GROUNDING RULES:\n" +
                "1. NEVER invent or fabricate senior projects or project counts.\n" +
                "2. All answers must strictly rely on the provided Senior Project Knowledge Base and Database Statistics.\n" +
                "3. For senior project similarity inquiries:\n" +
                "   - If a relevant senior project is present in the context, explain:\n" +
                "     * Existing project title\n" +
                "     * Batch\n" +
                "     * Authors (if available)\n" +
                "     * Why it is similar to the requested idea\n" +
                "     * Possible differences or uniqueness\n" +
                "     * Constructive recommendation for the current student to differentiate their project.\n" +
                "   - If NO sufficiently similar project exists in the retrieved context, explicitly say:\n" +
                "     \"I couldn't find a sufficiently similar project in the current senior-project database.\"\n" +
                "     NEVER say \"No one in the college has ever done this\", because the knowledge base may be incomplete.\n" +
                "4. For project count questions, use ONLY the exact numbers provided in the Database Statistics.\n" +
                "5. Maintain a professional, concise, and academic tone.";
    }

    private String buildContextPrompt(String userMessage, Map<String, Object> stats, List<RagSearchResultDto> relevantDocs, boolean isRag, boolean isStats) {
        StringBuilder sb = new StringBuilder();
        sb.append("User Query: \"").append(userMessage).append("\"\n\n");

        if (isStats && stats != null) {
            sb.append("--- DATABASE STATISTICS (EXACT SQL COUNTS) ---\n");
            sb.append("Total Projects Uploaded: ").append(stats.get("totalProjects")).append("\n");
            sb.append("Total Registered Students: ").append(stats.get("totalStudents")).append("\n");
            sb.append("Approved Projects: ").append(stats.get("approvedProjects")).append("\n");
            sb.append("Submitted/Pending Projects: ").append(stats.get("submittedProjects")).append("\n");
            sb.append("Rejected Projects: ").append(stats.get("rejectedProjects")).append("\n");
            sb.append("Projects Submitted This Month: ").append(stats.get("projectsThisMonth")).append("\n");
            if (stats.containsKey("facultyOnlyProjects")) {
                sb.append("Faculty-Only Source Code Projects: ").append(stats.get("facultyOnlyProjects")).append("\n");
            }
            sb.append("\n");
        }

        if (isRag) {
            sb.append("--- RETRIEVED SENIOR PROJECTS (RAG KNOWLEDGE BASE) ---\n");
            if (relevantDocs == null || relevantDocs.isEmpty()) {
                sb.append("NO SUFFICIENTLY SIMILAR SENIOR PROJECTS FOUND IN THE DATABASE.\n");
            } else {
                for (RagSearchResultDto doc : relevantDocs) {
                    sb.append("PROJECT TITLE: ").append(doc.getTitle()).append("\n");
                    sb.append("BATCH: ").append(doc.getBatch()).append("\n");
                    sb.append("AUTHORS: ").append(doc.getAuthors()).append("\n");
                    sb.append("DESCRIPTION: ").append(doc.getDescription()).append("\n");
                    sb.append("TECHNOLOGIES: ").append(doc.getTechnologies()).append("\n");
                    sb.append("SIMILARITY SCORE: ").append(String.format("%.2f", doc.getSimilarityScore())).append("\n\n");
                }
            }
        }

        sb.append("Please answer the user's inquiry based strictly on the above data.");
        return sb.toString();
    }

    private String buildFallbackResponse(String userMessage, Map<String, Object> stats, List<RagSearchResultDto> relevantDocs, boolean isRag, boolean isStats) {
        StringBuilder sb = new StringBuilder();

        if (isStats && stats != null) {
            sb.append("Here are the current institutional project statistics:\n");
            sb.append("• Total Projects: ").append(stats.get("totalProjects")).append("\n");
            sb.append("• Registered Students: ").append(stats.get("totalStudents")).append("\n");
            sb.append("• Approved Projects: ").append(stats.get("approvedProjects")).append("\n");
            sb.append("• Submitted (Pending Review): ").append(stats.get("submittedProjects")).append("\n");
            sb.append("• Projects Submitted This Month: ").append(stats.get("projectsThisMonth")).append("\n");
            if (stats.containsKey("facultyOnlyProjects")) {
                sb.append("• Faculty-Only Projects: ").append(stats.get("facultyOnlyProjects")).append("\n");
            }
            sb.append("\n");
        }

        if (isRag) {
            if (relevantDocs == null || relevantDocs.isEmpty()) {
                sb.append("I couldn't find a sufficiently similar project in the current senior-project database.");
            } else {
                RagSearchResultDto top = relevantDocs.get(0);
                sb.append("Yes, a related project was found in the senior-project database:\n\n");
                sb.append("• Project Title: ").append(top.getTitle()).append("\n");
                sb.append("• Batch: ").append(top.getBatch()).append("\n");
                sb.append("• Authors: ").append(top.getAuthors()).append("\n");
                sb.append("• Description: ").append(top.getDescription()).append("\n");
                sb.append("• Technologies: ").append(top.getTechnologies()).append("\n\n");
                sb.append("Why it is similar: It addresses a comparable domain and technical focus (").append(top.getTechnologies()).append(").\n\n");
                sb.append("Recommendation: To differentiate your project, consider introducing unique institutional integrations, an advanced algorithmic technique, or targeting a distinct operational workflow.");
            }
        }

        if (!groqService.isApiKeyConfigured()) {
            sb.append("\n\n*(Grounded response generated via local database & RAG engine. Set GROQ_API_KEY to activate Groq LLM synthesis.)*");
        }

        return sb.toString();
    }
}
