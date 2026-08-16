package com.meetingops.application.dto.request;

import com.meetingops.domain.enumeration.ReviewDecisionAction;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * Request payload for bulk review operations in the review queue.
 *
 * <p>Enforces safety rule: items flagged as {@code CONFLICT_DETECTED} are excluded
 * from bulk approval and require single-item review.</p>
 *
 * @param draftActionIds list of draft action UUIDs to review
 * @param decision       decision to apply (APPROVE, REJECT, BULK_APPROVE)
 * @param reason         optional rationale for audit trail
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public record BulkReviewDecisionRequest(
        @NotEmpty(message = "Draft action IDs list cannot be empty")
        List<UUID> draftActionIds,

        @NotNull(message = "Decision action must be specified")
        ReviewDecisionAction decision,

        String reason
) {
}
