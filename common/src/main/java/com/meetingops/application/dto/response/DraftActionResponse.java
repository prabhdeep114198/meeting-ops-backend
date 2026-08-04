package com.meetingops.application.dto.response;

import com.meetingops.domain.enumeration.DraftActionStatus;
import com.meetingops.domain.enumeration.DraftActionType;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for draft actions in the review queue.
 *
 * @param id               draft action identifier
 * @param extractedItemId  the extracted item this action was derived from
 * @param meetingId        the meeting context
 * @param actionType       type of action (TASK, CALENDAR_REMINDER, EMAIL)
 * @param payloadJson      current action payload (may be edited)
 * @param status           current lifecycle status
 * @param isAIGenerated    whether the content was AI-generated (for visual distinction per DATA-2)
 * @param createdAt        creation timestamp
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public record DraftActionResponse(
        UUID id,
        UUID extractedItemId,
        UUID meetingId,
        DraftActionType actionType,
        String payloadJson,
        DraftActionStatus status,
        boolean isAIGenerated,
        Instant createdAt
) {
}
