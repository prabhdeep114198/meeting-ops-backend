package com.meetingops.domain.model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port interface for draft action persistence in the domain layer.
 *
 * <p>Supports queries for the review queue with tenant isolation and
 * status-based filtering for the HITL workflow.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface DraftActionRepository {

    /**
     * Saves a draft action.
     *
     * @param draftAction the draft action to save
     * @return the persisted draft action
     */
    DraftAction save(DraftAction draftAction);

    /**
     * Finds a draft action by its unique identifier.
     *
     * @param id the draft action identifier
     * @return the draft action if found
     */
    Optional<DraftAction> findById(UUID id);

    /**
     * Finds all draft actions for a given meeting.
     *
     * @param meetingId the meeting identifier
     * @return list of draft actions for the meeting
     */
    List<DraftAction> findByMeetingId(UUID meetingId);

    /**
     * Finds all draft actions for an organization with DRAFTED status (review queue).
     * Enforces tenant isolation.
     *
     * @param organizationId the organization identifier
     * @return list of draft actions pending review
     */
    List<DraftAction> findPendingReviewByOrganizationId(UUID organizationId);

    /**
     * Saves a batch of draft actions.
     *
     * @param draftActions the draft actions to save
     * @return the persisted draft actions
     */
    List<DraftAction> saveAll(List<DraftAction> draftActions);
}
