package com.projecthub;

import com.projecthub.ai.embedding.EmbeddingService;
import com.projecthub.ai.rag.RagService;
import com.projecthub.ai.rag.SeniorProjectsParser;
import com.projecthub.dto.RagSearchResultDto;
import com.projecthub.entity.KnowledgeDocument;
import com.projecthub.repository.KnowledgeDocumentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class RagEngineTests {

    @Autowired
    private SeniorProjectsParser parser;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private RagService ragService;

    @Autowired
    private KnowledgeDocumentRepository knowledgeRepository;

    @Test
    void testParserHandlesEmptyInputGracefully() {
        List<KnowledgeDocument> docs = parser.parse("");
        assertNotNull(docs);
        assertTrue(docs.isEmpty());

        List<KnowledgeDocument> nullDocs = parser.parse(null);
        assertNotNull(nullDocs);
        assertTrue(nullDocs.isEmpty());
    }

    @Test
    void testParserWithHumanReadableSeniorProjectRecords() {
        String input = """
                PROJECT
                Title: Campus Mate
                Batch: A06
                Authors: Michael Scott, Jim Halpert
                Description: AI based college assistance chatbot system for students.
                Technologies: Java, Spring, AI, MySQL
                ---
                PROJECT
                Title: AI Interview Simulation
                Batch: A07
                Authors: Dwight Schrute
                Description: AI based interview simulation and speech analysis system.
                Technologies: Java, Python, LLM
                ---
                """;

        List<KnowledgeDocument> docs = parser.parse(input);
        assertEquals(2, docs.size());

        KnowledgeDocument doc1 = docs.get(0);
        assertEquals("Campus Mate", doc1.getTitle());
        assertEquals("A06", doc1.getBatch());
        assertEquals("Michael Scott, Jim Halpert", doc1.getAuthors());
        assertTrue(doc1.getDescription().contains("college assistance chatbot"));

        KnowledgeDocument doc2 = docs.get(1);
        assertEquals("AI Interview Simulation", doc2.getTitle());
        assertEquals("A07", doc2.getBatch());
    }

    @Test
    void testEmbeddingCosineSimilarity() {
        float[] v1 = embeddingService.generateEmbedding("college chatbot artificial intelligence");
        float[] v2 = embeddingService.generateEmbedding("chatbot college assistant AI system");
        float[] v3 = embeddingService.generateEmbedding("mechanical thermodynamics heat engine fluid dynamics");

        double simRelated = embeddingService.cosineSimilarity(v1, v2);
        double simUnrelated = embeddingService.cosineSimilarity(v1, v3);

        assertTrue(simRelated > 0.15, "Related texts should exhibit significant cosine similarity");
        assertTrue(simRelated > simUnrelated, "Related similarity must exceed unrelated similarity");
    }

    @Test
    void testRagSearchRetrieval() {
        knowledgeRepository.deleteAll();

        // Populate two test knowledge documents
        KnowledgeDocument d1 = new KnowledgeDocument();
        d1.setTitle("Campus Chatbot Mate");
        d1.setBatch("A06");
        d1.setAuthors("Jane Doe");
        d1.setDescription("An intelligent institutional chatbot to help college students find classrooms and schedules.");
        d1.setTechnologies("Java, Spring, AI");
        d1.setNormalizedContent("Campus Chatbot Mate. An intelligent institutional chatbot to help college students find classrooms and schedules. Technologies: Java, Spring, AI. Batch: A06.");
        d1.setEmbeddingJson(embeddingService.serializeVector(embeddingService.generateEmbedding(d1.getNormalizedContent())));
        knowledgeRepository.save(d1);

        KnowledgeDocument d2 = new KnowledgeDocument();
        d2.setTitle("Blockchain Land Registry");
        d2.setBatch("A05");
        d2.setAuthors("John Doe");
        d2.setDescription("Decentralized immutable ledger for municipal land parcel records.");
        d2.setTechnologies("Solidity, Web3");
        d2.setNormalizedContent("Blockchain Land Registry. Decentralized immutable ledger for municipal land parcel records. Technologies: Solidity, Web3. Batch: A05.");
        d2.setEmbeddingJson(embeddingService.serializeVector(embeddingService.generateEmbedding(d2.getNormalizedContent())));
        knowledgeRepository.save(d2);

        List<RagSearchResultDto> results = ragService.searchSimilar("Has anyone made a college chatbot project?", 2);

        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals("Campus Chatbot Mate", results.get(0).getTitle());
        assertTrue(results.get(0).getSimilarityScore() > 0.15);
        assertTrue(results.get(0).getSimilarityScore() > results.get(1).getSimilarityScore());
    }
}
