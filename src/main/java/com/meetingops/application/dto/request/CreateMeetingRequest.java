package com.meetingops.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

/**
 * Request DTO for creating a new meeting with transcript.
 *
 * @param title        meeting title (required)
 * @param meetingDate  date/time of the meeting (required)
 * @param attendees    list of attendee identifiers
 * @param transcript   raw transcript text (required)
 * @param teamId       optional team/project identifier
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public record CreateMeetingRequest(
        @NotBlank(message = "Meeting title is required")
        String title,

        @NotNull(message = "Meeting date is required")
        Instant meetingDate,

        @Size(max = 50, message = "Maximum 50 attendees allowed")
        List<String> attendees,

        @NotBlank(message = "Transcript text is required")
        String transcript,

        String teamId
) {
}
