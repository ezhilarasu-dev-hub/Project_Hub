package com.projecthub.ai.embedding;

public interface EmbeddingService {

    /**
     * Generates a normalized vector embedding for the input text.
     */
    float[] generateEmbedding(String text);

    /**
     * Calculates the cosine similarity between two vector embeddings.
     */
    double cosineSimilarity(float[] vectorA, float[] vectorB);

    /**
     * Serializes a float array to a compact JSON string for SQLite storage.
     */
    String serializeVector(float[] vector);

    /**
     * Deserializes a JSON string back to a float array.
     */
    float[] deserializeVector(String json);
}
