package com.meetingops.meeting.service;

import com.meetingops.application.dto.request.CreateMeetingRequest;
import com.meetingops.application.dto.response.MeetingResponse;

import java.util.List;
import java.util.UUID;

/**
 * Application service interface for meeting ingestion and management.
 *
 * <p>Handles the creation of meeting records, transcript storage, and
 * the publication of the {@code meeting.transcribed} event to trigger
 * the downstream agent pipeline.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface MeetingService {

    /**
     * Creates a new meeting with transcript, publishes the transcribed event,
     * and returns the meeting response.
     *
     * @param organizationId the owning organization's identifier
     * @param request        the meeting creation request
     * @return the created meeting response with INGESTED status
     */
    MeetingResponse createMeeting(UUID organizationId, CreateMeetingRequest request);

    /**
     * Retrieves a meeting by its identifier with item and draft counts.
     *
     * @param id the meeting identifier
     * @return the meeting response
     */
    MeetingResponse getMeeting(UUID id);

    /**
     * Lists all meetings for a given organization.
     *
     * @param organizationId the organization identifier
     * @return list of meeting responses
     */
    List<MeetingResponse> listMeetings(UUID organizationId);

    /**
     * Retrieves extracted items for a specific meeting with grounding information.
     *
     * @param meetingId the meeting identifier
     * @return list of extracted item responses
     */
    List<com.meetingops.application.dto.response.ExtractedItemResponse> getMeetingItems(UUID meetingId);
}
