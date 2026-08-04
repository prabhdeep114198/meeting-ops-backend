package com.meetingops.domain.model;

import java.util.List;
import java.util.UUID;

/**
 * Port interface for vector store operations in the domain layer.
 *
 * <p>Abstracts the embedding storage and retrieval for RAG-based
 * historical grounding. Implementations must enforce tenant isolation
 * at the data-access layer (DATA-3).</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface VectorStorePort {

    /**
     * Stores a text embedding in the tenant's vector namespace.
     *
     * @param organizationId the tenant identifier for namespace isolation
     * @param metadataId     the metadata identifier for retrieval (e.g., extracted item ID)
     * @param text           the text content to embed
     * @return the stored embedding ID
     */
    String storeEmbedding(UUID organizationId, UUID metadataId, String text);

    /**
     * Performs a similarity search within the tenant's vector namespace.
     *
     * @param organizationId the tenant identifier for namespace isolation
     * @param query          the query text
     * @param topK           the number of most similar results to return
     * @return list of similarity search results with scores
     */
    List<EmbeddingSearchResult> searchSimilar(UUID organizationId, String query, int topK);

    /**
     * Deletes embeddings associated with a specific metadata ID.
     *
     * @param organizationId the tenant identifier
     * @param metadataId     the metadata identifier to delete
     */
    void deleteEmbeddings(UUID organizationId, UUID metadataId);

    /**
     * Represents a single result from a similarity search.
     *
     * @param metadataId the metadata identifier of the matched document
     * @param text       the text content of the matched document
     * @param score      the similarity score (higher = more similar)
     */
    record EmbeddingSearchResult(UUID metadataId, String text, double score) {
    }
}
