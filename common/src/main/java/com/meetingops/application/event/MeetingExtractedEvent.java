package com.meetingops.application.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when structured extraction and pgvector grounding complete.
 * Published to Kafka topic {@code meeting.extracted}.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public record MeetingExtractedEvent(
        UUID meetingId,
        UUID organizationId,
        int actionItemCount,
        int decisionCount,
        Instant publishedAt
) {
    public static MeetingExtractedEvent of(UUID meetingId,
                                           UUID organizationId,
                                           int actionItemCount,
                                           int decisionCount) {
        return new MeetingExtractedEvent(meetingId, organizationId, actionItemCount, decisionCount, Instant.now());
    }
}
