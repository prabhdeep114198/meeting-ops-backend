package com.meetingops.infrastructure.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for pgvector embedding vectors (`meeting_embeddings`).
 * Uses HNSW indexing for memory-efficient similarity search.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Entity
@Table(name = "meeting_embeddings", indexes = {
        @Index(name = "idx_embeddings_org", columnList = "organization_id"),
        @Index(name = "idx_embeddings_metadata", columnList = "organization_id, metadata_id")
})
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingEmbeddingEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "organization_id", nullable = false, columnDefinition = "uuid")
    private UUID organizationId;

    @Column(name = "metadata_id", nullable = false, columnDefinition = "uuid")
    private UUID metadataId;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
