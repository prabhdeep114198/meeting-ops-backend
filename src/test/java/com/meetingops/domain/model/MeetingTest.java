package com.meetingops.domain.model;

import com.meetingops.domain.enumeration.MeetingStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Meeting domain model.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
class MeetingTest {

    @Test
    void shouldCreateMeetingWithDefaultValues() {
        UUID orgId = UUID.randomUUID();
        Meeting meeting = Meeting.create(
                orgId,
                "Sprint Planning",
                Instant.now(),
                List.of("user-1", "user-2"),
                "transcript-ref"
        );

        assertNotNull(meeting.id());
        assertEquals(orgId, meeting.organizationId());
        assertEquals("Sprint Planning", meeting.title());
        assertEquals(MeetingStatus.INGESTED, meeting.status());
        assertNotNull(meeting.createdAt());
        assertNotNull(meeting.updatedAt());
        // createdAt and updatedAt should be very close (within 1 second)
        long diffMs = java.time.Duration.between(meeting.createdAt(), meeting.updatedAt()).toMillis();
        assertTrue(Math.abs(diffMs) < 1000, "createdAt and updatedAt should be within 1 second");
    }

    @Test
    void shouldTransitionStatus() {
        Meeting meeting = Meeting.create(
                UUID.randomUUID(),
                "Test Meeting",
                Instant.now(),
                List.of(),
                "ref"
        );

        assertEquals(MeetingStatus.INGESTED, meeting.status());

        Meeting processing = meeting.withStatus(MeetingStatus.PROCESSING);
        assertEquals(MeetingStatus.PROCESSING, processing.status());
        assertNotEquals(meeting.updatedAt(), processing.updatedAt());
    }

    @Test
    void shouldCreateDraftAction() {
        UUID itemId = UUID.randomUUID();
        UUID meetingId = UUID.randomUUID();

        DraftAction draft = DraftAction.create(
                itemId,
                meetingId,
                com.meetingops.domain.enumeration.DraftActionType.TASK,
                "{\"title\": \"Test\"}",
                "{\"title\": \"Test\"}"
        );

        assertNotNull(draft.id());
        assertEquals(itemId, draft.extractedItemId());
        assertEquals(meetingId, draft.meetingId());
        assertEquals(com.meetingops.domain.enumeration.DraftActionStatus.DRAFTED, draft.status());
        assertNotNull(draft.originalPayload());
        assertNotNull(draft.createdAt());
    }
}
