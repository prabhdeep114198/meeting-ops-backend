package com.meetingops.application.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Response payload for conversational meeting Q&A queries (FR-7.2, FR-7.3).
 *
 * @param sessionId      conversational session ID
 * @param answer         generated response grounded strictly in meeting history
 * @param citedExcerpts  list of cited transcript excerpts and timestamps
 * @param isGrounded     false if fallback was triggered because no relevant content was found
 * @param latencyMs      query latency in milliseconds
 *
 * @author MeetingOps Team
 * @since 2.0.0
 */
public record QAQueryResponse(
        UUID sessionId,
        String answer,
        List<CitationExcerpt> citedExcerpts,
        boolean isGrounded,
        long latencyMs
) {
    public record CitationExcerpt(
            UUID meetingId,
            String meetingTitle,
            String speakerLabel,
            long startTimeMs,
            long endTimeMs,
            String textSnippet
    ) {}
}
