package com.meetingops.application.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Descriptive, factual engagement metrics response for an individual speaker (FR-8.1, FR-8.4).
 * Strictly descriptive; no performance scores, ranks, or ratings exist (FR-8.3, FR-9.1).
 *
 * @author MeetingOps Team
 * @since 2.0.0
 */
public record EngagementMetricsResponse(
        UUID id,
        UUID meetingId,
        UUID speakerId,
        String speakerLabel,
        int talkTimeSeconds,
        double talkTimePct,
        int turnCount,
        int questionsAskedCount,
        Instant computedAt
) {
}
