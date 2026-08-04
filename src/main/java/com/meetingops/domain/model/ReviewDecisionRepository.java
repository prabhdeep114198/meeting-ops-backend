package com.meetingops.domain.model;

import java.util.List;
import java.util.UUID;

/**
 * Port interface for review decision (audit trail) persistence in the domain layer.
 *
 * <p>Every review decision is immutable and forms the audit trail for
 * compliance with FR-6.4 and NFR-6.1.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface ReviewDecisionRepository {

    /**
     * Saves a review decision (immutable audit record).
     *
     * @param decision the review decision to save
     * @return the persisted decision
     */
    ReviewDecision save(ReviewDecision decision);

    /**
     * Finds all review decisions for a specific draft action.
     *
     * @param draftActionId the draft action identifier
     * @return list of review decisions for the draft action
     */
    List<ReviewDecision> findByDraftActionId(UUID draftActionId);

    /**
     * Finds all review decisions by a specific reviewer.
     *
     * @param reviewerId the reviewer's user identifier
     * @return list of review decisions made by the reviewer
     */
    List<ReviewDecision> findByReviewerId(UUID reviewerId);
}
