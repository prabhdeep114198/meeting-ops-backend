package com.meetingops.application.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Descriptive team-level meeting participation summary response (FR-8.2, FR-8.5).
 *
 * @author MeetingOps Team
 * @since 2.0.0
 */
public record ParticipationSummaryResponse(
        UUID id,
        UUID meetingId,
        int participantCount,
        int totalTalkTimeSeconds,
        String balanceIndicator,
        Double giniCoefficient,
        Double topTwoTalkPct,
        String descriptiveNotes,
        Instant computedAt
) {
}
