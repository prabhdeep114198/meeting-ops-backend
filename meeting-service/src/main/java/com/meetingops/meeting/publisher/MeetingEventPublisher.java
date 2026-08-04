package com.meetingops.meeting.publisher;

import com.meetingops.application.event.MeetingTranscribedEvent;
import com.meetingops.infrastructure.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Kafka event publisher for meeting transcription events.
 *
 * <p>Publishes the {@code meeting.transcribed} event (FR-1.3) to Kafka
 * after successful transcript ingestion, decoupling ingestion from
 * the downstream agent pipeline.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publishes a meeting transcription event to Kafka.
     *
     * @param event the meeting transcription event
     */
    public void publishMeetingTranscribed(final MeetingTranscribedEvent event) {
        // TODO: Add proper serialization and partition key (organizationId)
        log.info("Publishing meeting.transcribed event for meeting={}", event.meetingId());
        kafkaTemplate.send(
                KafkaConfig.MEETING_TRANSCRIBED_TOPIC,
                event.meetingId().toString(),
                event
        );
    }
}
