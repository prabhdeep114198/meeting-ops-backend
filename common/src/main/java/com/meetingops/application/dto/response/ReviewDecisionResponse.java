package com.meetingops.application.dto.response;

import com.meetingops.domain.enumeration.AuditAction;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for review decisions in the audit trail.
 *
 * @param id              decision identifier
 * @param draftActionId   the draft action being reviewed
 * @param reviewerId      the reviewer who made the decision
 * @param action          the audit action type
 * @param originalPayload the original AI-generated payload
 * @param finalPayload    the final approved payload
 * @param reason          reviewer's explanation
 * @param timestamp       decision timestamp
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public record ReviewDecisionResponse(
        UUID id,
        UUID draftActionId,
        UUID reviewerId,
        AuditAction action,
        String originalPayload,
        String finalPayload,
        String reason,
        Instant timestamp
) {
}
