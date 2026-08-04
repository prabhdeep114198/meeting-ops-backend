package com.meetingops.domain.model;

import com.meetingops.domain.enumeration.AuditAction;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model representing a human reviewer's decision on a draft action.
 *
 * <p>Every review decision is immutable and persisted for audit compliance.
 * It records the actor, timestamp, original draft content, final approved
 * content (if edited), and the decision reason.</p>
 *
 * @param id              unique identifier
 * @param draftActionId   the draft action being reviewed
 * @param reviewerId      the user who made the decision
 * @param action          the type of audit action (APPROVE, EDIT_AND_APPROVE, REJECT, EXECUTE)
 * @param originalPayload the original AI-generated draft payload
 * @param finalPayload    the final approved payload (may differ from original if edited)
 * @param reason          reviewer's explanation for the decision
 * @param timestamp       when the decision was made
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public record ReviewDecision(
        UUID id,
        UUID draftActionId,
        UUID reviewerId,
        AuditAction action,
        String originalPayload,
        String finalPayload,
        String reason,
        Instant timestamp
) {
    /**
     * Creates a new ReviewDecision with auto-generated UUID.
     *
     * @param draftActionId   the draft action being reviewed
     * @param reviewerId      the user who made the decision
     * @param action          the type of audit action
     * @param originalPayload the original AI-generated draft payload
     * @param finalPayload    the final approved payload
     * @param reason          reviewer's explanation
     * @return a new ReviewDecision
     */
    public static ReviewDecision create(final UUID draftActionId,
                                        final UUID reviewerId,
                                        final AuditAction action,
                                        final String originalPayload,
                                        final String finalPayload,
                                        final String reason) {
        return new ReviewDecision(
                UUID.randomUUID(),
                draftActionId,
                reviewerId,
                action,
                originalPayload,
                finalPayload,
                reason,
                Instant.now()
        );
    }
}
