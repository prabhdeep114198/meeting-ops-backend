package com.meetingops.application.event;

import com.meetingops.domain.enumeration.DraftActionStatus;
import com.meetingops.domain.enumeration.ReviewDecisionAction;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when human reviewer submits a decision on a draft action.
 * Published to Kafka topic {@code action.decided} (FR-6.2).
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public record ActionDecidedEvent(
        UUID draftActionId,
        UUID meetingId,
        UUID organizationId,
        UUID reviewerId,
        ReviewDecisionAction decision,
        DraftActionStatus resultingStatus,
        Instant publishedAt
) {
    public static ActionDecidedEvent of(UUID draftActionId,
                                        UUID meetingId,
                                        UUID organizationId,
                                        UUID reviewerId,
                                        ReviewDecisionAction decision,
                                        DraftActionStatus resultingStatus) {
        return new ActionDecidedEvent(draftActionId, meetingId, organizationId, reviewerId, decision, resultingStatus, Instant.now());
    }
}
