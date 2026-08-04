package com.meetingops.domain.model;

import com.meetingops.domain.enumeration.MeetingStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Domain model representing a meeting record with its transcript and processing state.
 *
 * <p>A Meeting is the central entity in the platform. It holds the raw transcript,
 * metadata (title, date, attendees, team), and the current processing status.
 * Extracted items and draft actions are associated with a meeting via foreign references.</p>
 *
 * @param id            unique identifier
 * @param organizationId the owning organization's identifier
 * @param teamId        optional team/project identifier
 * @param title         meeting title
 * @param meetingDate   date/time of the meeting
 * @param attendees     list of attendee identifiers
 * @param transcriptRef reference to the stored transcript content
 * @param status        current processing status in the pipeline
 * @param createdAt     timestamp of creation
 * @param updatedAt     timestamp of last update
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public record Meeting(
        UUID id,
        UUID organizationId,
        UUID teamId,
        String title,
        Instant meetingDate,
        List<String> attendees,
        String transcriptRef,
        MeetingStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    /**
     * Creates a new Meeting with auto-generated UUID and INGESTED status.
     *
     * @param organizationId the owning organization's identifier
     * @param title          meeting title
     * @param meetingDate    date/time of the meeting
     * @param attendees      list of attendee identifiers
     * @param transcriptRef  reference to the stored transcript content
     * @return a new Meeting instance with INGESTED status
     */
    public static Meeting create(final UUID organizationId,
                                 final String title,
                                 final Instant meetingDate,
                                 final List<String> attendees,
                                 final String transcriptRef) {
        return new Meeting(
                UUID.randomUUID(),
                organizationId,
                null,
                title,
                meetingDate,
                attendees,
                transcriptRef,
                MeetingStatus.INGESTED,
                Instant.now(),
                Instant.now()
        );
    }

    /**
     * Returns a copy of this meeting with updated status.
     *
     * @param newStatus the new processing status
     * @return a new Meeting instance with updated status and timestamp
     */
    public Meeting withStatus(final MeetingStatus newStatus) {
        return new Meeting(
                this.id, this.organizationId, this.teamId, this.title,
                this.meetingDate, this.attendees, this.transcriptRef,
                newStatus, this.createdAt, Instant.now()
        );
    }
}
