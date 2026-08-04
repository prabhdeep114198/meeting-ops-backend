package com.meetingops.meeting.controller;

import com.meetingops.application.dto.request.CreateMeetingRequest;
import com.meetingops.application.dto.response.ExtractedItemResponse;
import com.meetingops.application.dto.response.MeetingResponse;
import com.meetingops.meeting.service.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for meeting ingestion and management endpoints.
 *
 * <p>Implements the API interfaces defined in Section 4.2 of the SRS:
 * <ul>
 *   <li>{@code POST /api/v1/meetings} — Create meeting + upload transcript</li>
 *   <li>{@code GET /api/v1/meetings/{id}} — Meeting detail and status</li>
 *   <li>{@code GET /api/v1/meetings/{id}/items} — Extracted items for a meeting</li>
 * </ul>
 * </p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    /**
     * Creates a new meeting with transcript.
     *
     * @param request the meeting creation request
     * @return the created meeting response (201 Created)
     */
    @PostMapping
    public ResponseEntity<MeetingResponse> createMeeting(
            @Valid @RequestBody final CreateMeetingRequest request) {
        // TODO: Extract organizationId from authenticated user context (JWT)
        UUID organizationId = UUID.randomUUID(); // Placeholder
        MeetingResponse response = meetingService.createMeeting(organizationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a meeting by its identifier.
     *
     * @param id the meeting identifier
     * @return the meeting response
     */
    @GetMapping("/{id}")
    public ResponseEntity<MeetingResponse> getMeeting(@PathVariable final UUID id) {
        MeetingResponse response = meetingService.getMeeting(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Lists all meetings for the authenticated user's organization.
     *
     * @return list of meeting responses
     */
    @GetMapping
    public ResponseEntity<List<MeetingResponse>> listMeetings() {
        // TODO: Extract organizationId from authenticated user context
        UUID organizationId = UUID.randomUUID(); // Placeholder
        List<MeetingResponse> meetings = meetingService.listMeetings(organizationId);
        return ResponseEntity.ok(meetings);
    }

    /**
     * Retrieves extracted items for a specific meeting.
     *
     * @param id the meeting identifier
     * @return list of extracted item responses with grounding information
     */
    @GetMapping("/{id}/items")
    public ResponseEntity<List<ExtractedItemResponse>> getMeetingItems(
            @PathVariable final UUID id) {
        List<ExtractedItemResponse> items = meetingService.getMeetingItems(id);
        return ResponseEntity.ok(items);
    }
}
