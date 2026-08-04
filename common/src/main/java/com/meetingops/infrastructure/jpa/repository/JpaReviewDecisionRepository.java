package com.meetingops.infrastructure.jpa.repository;

import com.meetingops.infrastructure.jpa.entity.ReviewDecisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for review decision entities.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface JpaReviewDecisionRepository extends JpaRepository<ReviewDecisionEntity, UUID> {

    /**
     * Finds all review decisions for a specific draft action (audit trail).
     *
     * @param draftActionId the draft action identifier
     * @return list of review decision entities
     */
    List<ReviewDecisionEntity> findByDraftActionId(UUID draftActionId);

    /**
     * Finds all review decisions by a specific reviewer.
     *
     * @param reviewerId the reviewer's user identifier
     * @return list of review decision entities
     */
    List<ReviewDecisionEntity> findByReviewerId(UUID reviewerId);
}
