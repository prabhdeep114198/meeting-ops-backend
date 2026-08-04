package com.meetingops.application.dto.response;

import com.meetingops.domain.enumeration.GroundingClassification;
import com.meetingops.domain.enumeration.ItemStatus;
import com.meetingops.domain.enumeration.ItemType;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for extracted items with their grounding classification.
 *
 * @param id                  item identifier
 * @param type                item type (ACTION_ITEM or DECISION)
 * @param description         extracted description
 * @param owner               identified owner (null if ambiguous)
 * @param deadline            identified deadline (null if ambiguous)
 * @param supportingExcerpt   supporting transcript excerpt
 * @param confidence          confidence score
 * @param status              processing status
 * @param groundingResult     grounding classification result
 * @param groundingRationale  plain-language explanation of grounding
 * @param createdAt           creation timestamp
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public record ExtractedItemResponse(
        UUID id,
        ItemType type,
        String description,
        String owner,
        String deadline,
        String supportingExcerpt,
        Double confidence,
        ItemStatus status,
        GroundingClassification groundingResult,
        String groundingRationale,
        Instant createdAt
) {
}
