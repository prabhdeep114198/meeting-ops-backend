package com.meetingops.infrastructure.jpa.repository;

import com.meetingops.infrastructure.jpa.entity.GroundingResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for grounding result entities.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface JpaGroundingResultRepository extends JpaRepository<GroundingResultEntity, UUID> {

    /**
     * Finds a grounding result for a specific extracted item.
     *
     * @param extractedItemId the extracted item identifier
     * @return the grounding result entity if found
     */
    Optional<GroundingResultEntity> findByExtractedItemId(UUID extractedItemId);
}
