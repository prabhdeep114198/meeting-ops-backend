package com.meetingops.infrastructure.jpa.repository;

import com.meetingops.infrastructure.jpa.entity.MeetingEmbeddingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for meeting vector embeddings.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface JpaMeetingEmbeddingRepository extends JpaRepository<MeetingEmbeddingEntity, UUID> {

    List<MeetingEmbeddingEntity> findByOrganizationId(UUID organizationId);

    List<MeetingEmbeddingEntity> findByOrganizationIdAndMetadataId(UUID organizationId, UUID metadataId);
}
