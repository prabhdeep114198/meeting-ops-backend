package com.meetingops.application.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * Request payload for conversational meeting Q&A queries (FR-7.1, FR-7.5).
 *
 * @param meetingId optional meeting ID to scope query to a single meeting
 * @param sessionId optional session ID for multi-turn conversational context
 * @param question  user natural language question
 * @param scope     scope of search: SINGLE_MEETING, TEAM_MEETINGS, ORG_HISTORY
 *
 * @author MeetingOps Team
 * @since 2.0.0
 */
public record QAQueryRequest(
        UUID meetingId,
        UUID sessionId,
        @NotBlank(message = "Question text cannot be blank")
        String question,
        String scope
) {
}
