package com.meetingops.infrastructure.persistence;

import com.meetingops.application.event.MeetingTranscribedEvent;
import com.meetingops.application.service.AgentPipelineService;
import com.meetingops.infrastructure.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka event consumer for meeting transcription events.
 *
 * <p>Listens for {@code meeting.transcribed} events on Kafka and
 * triggers the agent pipeline to process the meeting through
 * extraction → grounding → validation → drafting.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingEventConsumer {

    private final AgentPipelineService agentPipelineService;

    /**
     * Consumes meeting transcription events from Kafka.
     *
     * @param event the meeting transcription event
     */
    @KafkaListener(topics = KafkaConfig.MEETING_TRANSCRIBED_TOPIC, groupId = "meeting-ops-agent")
    public void onMeetingTranscribed(final MeetingTranscribedEvent event) {
        log.info("Received meeting.transcribed event for meeting={}", event.meetingId());

        try {
            // TODO: Add retry logic with exponential backoff (max 3 attempts per NFR-3.2)
            agentPipelineService.processMeeting(event.meetingId());
        } catch (Exception e) {
            log.error("Failed to process meeting {}: {}", event.meetingId(), e.getMessage(), e);
            // TODO: Mark meeting as PROCESSING_FAILED and surface for manual intervention
        }
    }
}
