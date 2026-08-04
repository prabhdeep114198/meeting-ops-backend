package com.meetingops.application.event;

import com.meetingops.domain.enumeration.AuditAction;
import com.meetingops.domain.enumeration.DraftActionStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a reviewer makes a decision on a draft action.
 *
 * <p>Triggered after a review decision is persisted. Downstream consumers
 * can react to execute the approved action against external systems.</p>
 *
 * @param draftActionId the draft action identifier
 * @param reviewerId    the reviewer who made the decision
 * @param action        the audit action type
 * @param newStatus     the resulting draft action status
 * @param publishedAt   timestamp of event publication
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public record ReviewDecisionEvent(
        UUID draftActionId,
        UUID reviewerId,
        AuditAction action,
        DraftActionStatus newStatus,
        Instant publishedAt
) {
    /**
     * Creates a new ReviewDecisionEvent.
     *
     * @param draftActionId the draft action identifier
     * @param reviewerId    the reviewer identifier
     * @param action        the audit action type
     * @param newStatus     the resulting status
     * @return a new event with current timestamp
     */
    public static ReviewDecisionEvent of(final UUID draftActionId,
                                         final UUID reviewerId,
                                         final AuditAction action,
                                         final DraftActionStatus newStatus) {
        return new ReviewDecisionEvent(draftActionId, reviewerId, action, newStatus, Instant.now());
    }
}
