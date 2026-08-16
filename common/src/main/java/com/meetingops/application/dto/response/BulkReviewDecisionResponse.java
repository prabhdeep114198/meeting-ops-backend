package com.meetingops.application.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Response payload for bulk review operations.
 *
 * @param processedCount       number of items successfully approved/processed
 * @param skippedConflictCount number of items blocked due to conflict flags
 * @param processedIds         list of processed action IDs
 * @param skippedIds           list of action IDs requiring manual single-item review
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public record BulkReviewDecisionResponse(
        int processedCount,
        int skippedConflictCount,
        List<UUID> processedIds,
        List<UUID> skippedIds
) {
}
