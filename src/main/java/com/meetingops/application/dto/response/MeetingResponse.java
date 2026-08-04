package com.meetingops.application.dto.response;

import com.meetingops.domain.enumeration.MeetingStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for meeting details and processing status.
 *
 * @param id            meeting identifier
 * @param title         meeting title
 * @param meetingDate   date/time of the meeting
 * @param attendees     list of attendee identifiers
 * @param status        current processing status
 * @param createdAt     creation timestamp
 * @param updatedAt     last update timestamp
 * @param itemCount     number of extracted items for this meeting
 * @param draftCount    number of draft actions for this meeting
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public record MeetingResponse(
        UUID id,
        String title,
        Instant meetingDate,
        List<String> attendees,
        MeetingStatus status,
        Instant createdAt,
        Instant updatedAt,
        int itemCount,
        int draftCount
) {
}
