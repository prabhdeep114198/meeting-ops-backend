package com.meetingops.application.event;

import com.meetingops.domain.enumeration.MeetingCaptureMode;
import com.meetingops.domain.enumeration.MeetingPlatform;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when meeting audio capture completes.
 * Published to Kafka topic {@code meeting.captured} (FR-1.6).
 *
 * @author MeetingOps Team
 * @since 2.0.0
 */
public record MeetingCapturedEvent(
        UUID meetingId,
        UUID organizationId,
        String audioS3Uri,
        MeetingCaptureMode captureMode,
        MeetingPlatform platform,
        Instant publishedAt
) {
    public static MeetingCapturedEvent of(UUID meetingId,
                                          UUID organizationId,
                                          String audioS3Uri,
                                          MeetingCaptureMode captureMode,
                                          MeetingPlatform platform) {
        return new MeetingCapturedEvent(meetingId, organizationId, audioS3Uri, captureMode, platform, Instant.now());
    }
}
