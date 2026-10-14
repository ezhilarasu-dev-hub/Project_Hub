package com.projecthub.ai.embedding;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class LocalSemanticEmbeddingService implements EmbeddingService {

    private static final int VECTOR_DIMENSION = 512;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are",
            "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but",
            "by", "did", "do", "does", "doing", "down", "during", "each", "few", "for", "from", "further",
            "had", "has", "have", "having", "he", "her", "here", "hers", "herself", "him", "himself", "his",
            "how", "i", "if", "in", "into", "is", "it", "its", "itself", "just", "me", "more", "most", "my",
            "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "or", "other", "our", "ours",
            "ourselves", "out", "over", "own", "same", "she", "should", "so", "some", "such", "than", "that",
            "the", "their", "theirs", "them", "themselves", "then", "there", "these", "they", "this", "those",
            "through", "to", "too", "under", "until", "up", "very", "was", "we", "were", "what", "when",
            "where", "which", "while", "who", "whom", "why", "with", "would", "you", "your", "yours"
    ));

    @Override
    public float[] generateEmbedding(String text) {
        float[] vector = new float[VECTOR_DIMENSION];
        if (!StringUtils.hasText(text)) {
            return vector;
        }

        String normalized = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        String[] rawTokens = normalized.split("\\s+");

        List<String> validTokens = new ArrayList<>();
        for (String t : rawTokens) {
            if (t.length() > 1 && !STOP_WORDS.contains(t)) {
                validTokens.add(t);
            }
        }

        if (validTokens.isEmpty()) {
            return vector;
        }

        // 1. Unigram positive frequency weighting
        for (String token : validTokens) {
            int hash = Math.abs(token.hashCode());
            int index = hash % VECTOR_DIMENSION;
            vector[index] += 2.0f;

            // Character trigrams for root/stem overlap (e.g. "chat", "bot", "interv")
            if (token.length() >= 4) {
                for (int i = 0; i <= token.length() - 4; i++) {
                    String sub = token.substring(i, i + 4);
                    int subHash = Math.abs(sub.hashCode() * 37);
                    int subIndex = subHash % VECTOR_DIMENSION;
                    vector[subIndex] += 0.8f;
                }
            }
        }

        // 2. Bigrams for contextual phrases (e.g., "college_chatbot", "interview_system")
        for (int i = 0; i < validTokens.size() - 1; i++) {
            String bigram = validTokens.get(i) + "_" + validTokens.get(i + 1);
            int biHash = Math.abs(bigram.hashCode() * 7919);
            int biIndex = biHash % VECTOR_DIMENSION;
            vector[biIndex] += 3.0f;
        }

        // 3. L2 Unit Normalization: ||vector|| = 1
        double sumSquares = 0.0;
        for (float v : vector) {
            sumSquares += v * v;
        }

        if (sumSquares > 0.0) {
            float norm = (float) Math.sqrt(sumSquares);
            for (int i = 0; i < vector.length; i++) {
                vector[i] = vector[i] / norm;
            }
        }

        return vector;
    }

    @Override
    public double cosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA == null || vectorB == null || vectorA.length != vectorB.length) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        if (normA <= 0.0 || normB <= 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    @Override
    public String serializeVector(float[] vector) {
        try {
            return objectMapper.writeValueAsString(vector);
        } catch (Exception e) {
            return "[]";
        }
    }

    @Override
    public float[] deserializeVector(String json) {
        try {
            return objectMapper.readValue(json, float[].class);
        } catch (Exception e) {
            return new float[VECTOR_DIMENSION];
        }
    }
}
