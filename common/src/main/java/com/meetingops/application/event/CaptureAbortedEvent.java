package com.meetingops.application.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when meeting bot capture is aborted due to missing consent.
 * Published to Kafka topic {@code capture.aborted} (FR-1.3, PRIV-2).
 *
 * @author MeetingOps Team
 * @since 2.0.0
 */
public record CaptureAbortedEvent(
        UUID meetingId,
        UUID organizationId,
        String reason,
        int missingConsentCount,
        Instant abortedAt
) {
    public static CaptureAbortedEvent of(UUID meetingId,
                                         UUID organizationId,
                                         String reason,
                                         int missingConsentCount) {
        return new CaptureAbortedEvent(meetingId, organizationId, reason, missingConsentCount, Instant.now());
    }
}
