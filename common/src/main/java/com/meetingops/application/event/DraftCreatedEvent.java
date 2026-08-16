package com.meetingops.application.event;

import com.meetingops.domain.enumeration.DraftActionType;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when drafting agent generates FastMCP draft actions.
 * Published to Kafka topic {@code draft.created}.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public record DraftCreatedEvent(
        UUID draftActionId,
        UUID meetingId,
        UUID organizationId,
        DraftActionType actionType,
        Instant publishedAt
) {
    public static DraftCreatedEvent of(UUID draftActionId,
                                       UUID meetingId,
                                       UUID organizationId,
                                       DraftActionType actionType) {
        return new DraftCreatedEvent(draftActionId, meetingId, organizationId, actionType, Instant.now());
    }
}
