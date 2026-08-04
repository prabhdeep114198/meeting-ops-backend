package com.meetingops.meeting.service;

import com.meetingops.application.dto.request.CreateMeetingRequest;
import com.meetingops.application.dto.response.ExtractedItemResponse;
import com.meetingops.application.dto.response.MeetingResponse;
import com.meetingops.application.event.MeetingTranscribedEvent;
import com.meetingops.domain.model.*;
import com.meetingops.meeting.publisher.MeetingEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service implementation for meeting ingestion and management.
 *
 * <p>Coordinates the creation of meeting records, transcript storage,
 * and the publication of the {@code meeting.transcribed} event.
 * Operates within the application layer following Clean Architecture
 * principles.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final ExtractedItemRepository extractedItemRepository;
    private final GroundingResultRepository groundingResultRepository;
    private final MeetingEventPublisher eventPublisher;

    @Override
    @Transactional
    public MeetingResponse createMeeting(final UUID organizationId,
                                         final CreateMeetingRequest request) {
        log.info("Creating meeting '{}' for organization {}", request.title(), organizationId);

        // TODO: Store transcript content and get reference (S3/file system)
        String transcriptRef = "transcript-ref-" + UUID.randomUUID();

        Meeting meeting = Meeting.create(
                organizationId,
                request.title(),
                request.meetingDate(),
                request.attendees(),
                transcriptRef
        );

        meeting = meetingRepository.save(meeting);

        // Publish event to trigger agent pipeline (FR-1.3)
        eventPublisher.publishMeetingTranscribed(
                MeetingTranscribedEvent.of(meeting.id(), organizationId, transcriptRef)
        );

        return mapToResponse(meeting, 0, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public MeetingResponse getMeeting(final UUID id) {
        Meeting meeting = meetingRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Meeting not found: " + id));

        int itemCount = extractedItemRepository.findByMeetingId(id).size();
        int draftCount = 0; // TODO: Query draft actions for this meeting
        return mapToResponse(meeting, itemCount, draftCount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeetingResponse> listMeetings(final UUID organizationId) {
        return meetingRepository.findByOrganizationId(organizationId).stream()
                .map(meeting -> mapToResponse(meeting, 0, 0))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExtractedItemResponse> getMeetingItems(final UUID meetingId) {
        List<ExtractedItem> items = extractedItemRepository.findByMeetingId(meetingId);

        return items.stream().map(item -> {
            var groundingResult = groundingResultRepository.findByExtractedItemId(item.id())
                    .orElse(null);

            return new ExtractedItemResponse(
                    item.id(),
                    item.type(),
                    item.description(),
                    item.owner(),
                    item.deadline(),
                    item.supportingExcerpt(),
                    item.confidence(),
                    item.status(),
                    groundingResult != null ? groundingResult.classification() : null,
                    groundingResult != null ? groundingResult.rationale() : null,
                    item.createdAt()
            );
        }).collect(Collectors.toList());
    }

    /**
     * Maps a domain Meeting to a response DTO.
     */
    private MeetingResponse mapToResponse(final Meeting meeting,
                                          final int itemCount,
                                          final int draftCount) {
        return new MeetingResponse(
                meeting.id(),
                meeting.title(),
                meeting.meetingDate(),
                meeting.attendees(),
                meeting.status(),
                meeting.createdAt(),
                meeting.updatedAt(),
                itemCount,
                draftCount
        );
    }
}
