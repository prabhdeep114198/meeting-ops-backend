package com.meetingops.infrastructure.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Apache Kafka integration.
 *
 * <p>Kafka is used for decoupling meeting transcript ingestion from
 * the downstream agent pipeline. The {@code meeting.transcribed} event
 * (FR-1.3) is published to Kafka after successful transcript ingestion.</p>
 *
 * <p>Kafka configuration is managed via application.yml properties.
 * This class provides additional custom configuration if needed.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Configuration
public class KafkaConfig {

    /**
     * Kafka topic for meeting transcription events.
     */
    public static final String MEETING_TRANSCRIBED_TOPIC = "meeting.transcribed";

    /**
     * Kafka topic for review decision events.
     */
    public static final String REVIEW_DECISION_TOPIC = "review.decision";

    // TODO: Configure Kafka producer/consumer beans if needed
    // - Custom serializers/deserializers
    // - Consumer group configurations
    // - Dead letter queue configuration
}
