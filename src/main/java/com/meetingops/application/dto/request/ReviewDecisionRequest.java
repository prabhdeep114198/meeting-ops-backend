package com.meetingops.application.dto.request;

import com.meetingops.domain.enumeration.AuditAction;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for submitting a review decision on a draft action.
 *
 * @param action       the audit action (APPROVE, EDIT_AND_APPROVE, REJECT)
 * @param finalPayload the edited payload (required for EDIT_AND_APPROVE)
 * @param reason       reviewer's explanation for the decision
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public record ReviewDecisionRequest(
        @NotNull(message = "Review action is required")
        AuditAction action,

        String finalPayload,

        String reason
) {
}
