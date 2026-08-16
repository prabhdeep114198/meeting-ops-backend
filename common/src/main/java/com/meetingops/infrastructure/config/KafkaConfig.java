package com.meetingops.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

/**
 * Enterprise Apache Kafka event bus configuration for MeetingOps.
 *
 * <p>Provisions partitioned topics for the asynchronous multi-agent pipeline:
 * {@code meeting.captured}, {@code meeting.transcribed}, {@code meeting.extracted},
 * {@code draft.created}, and {@code action.decided}.</p>
 *
 * <p>Configures Dead Letter Queues (DLQ) and exponential backoff retry policies (FR-1.6, FR-2.5, NFR-3.3).</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Configuration
@ConditionalOnClass(KafkaTemplate.class)
public class KafkaConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    // =========================================================================
    // Topic Constants
    // =========================================================================

    /** Published when meeting audio is captured from a video call or uploaded manually. */
    public static final String MEETING_CAPTURED_TOPIC = "meeting.captured";
    public static final String MEETING_CAPTURED_DLQ = "meeting.captured.DLQ";

    /** Published when STT transcription and speaker diarization complete. */
    public static final String MEETING_TRANSCRIBED_TOPIC = "meeting.transcribed";
    public static final String MEETING_TRANSCRIBED_DLQ = "meeting.transcribed.DLQ";

    /** Published when structured action items and decisions are extracted and grounded. */
    public static final String MEETING_EXTRACTED_TOPIC = "meeting.extracted";
    public static final String MEETING_EXTRACTED_DLQ = "meeting.extracted.DLQ";

    /** Published when FastMCP draft action payloads (Jira/Calendar/Email) are generated. */
    public static final String DRAFT_CREATED_TOPIC = "draft.created";
    public static final String DRAFT_CREATED_DLQ = "draft.created.DLQ";

    /** Published when a human reviewer approves, edits, or rejects a draft action. */
    public static final String ACTION_DECIDED_TOPIC = "action.decided";
    public static final String ACTION_DECIDED_DLQ = "action.decided.DLQ";

    /** Legacy alias */
    public static final String REVIEW_DECISION_TOPIC = "review.decision";

    public static final int DEFAULT_PARTITIONS = 3;
    public static final short DEFAULT_REPLICATION_FACTOR = 1;

    // =========================================================================
    // Core Topic Definitions (3 Partitions each for Horizontal Scalability)
    // =========================================================================

    @Bean
    public NewTopic meetingCapturedTopic() {
        return TopicBuilder.name(MEETING_CAPTURED_TOPIC)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICATION_FACTOR)
                .build();
    }

    @Bean
    public NewTopic meetingCapturedDlqTopic() {
        return TopicBuilder.name(MEETING_CAPTURED_DLQ)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICATION_FACTOR)
                .build();
    }

    @Bean
    public NewTopic meetingTranscribedTopic() {
        return TopicBuilder.name(MEETING_TRANSCRIBED_TOPIC)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICATION_FACTOR)
                .build();
    }

    @Bean
    public NewTopic meetingTranscribedDlqTopic() {
        return TopicBuilder.name(MEETING_TRANSCRIBED_DLQ)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICATION_FACTOR)
                .build();
    }

    @Bean
    public NewTopic meetingExtractedTopic() {
        return TopicBuilder.name(MEETING_EXTRACTED_TOPIC)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICATION_FACTOR)
                .build();
    }

    @Bean
    public NewTopic meetingExtractedDlqTopic() {
        return TopicBuilder.name(MEETING_EXTRACTED_DLQ)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICATION_FACTOR)
                .build();
    }

    @Bean
    public NewTopic draftCreatedTopic() {
        return TopicBuilder.name(DRAFT_CREATED_TOPIC)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICATION_FACTOR)
                .build();
    }

    @Bean
    public NewTopic draftCreatedDlqTopic() {
        return TopicBuilder.name(DRAFT_CREATED_DLQ)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICATION_FACTOR)
                .build();
    }

    @Bean
    public NewTopic actionDecidedTopic() {
        return TopicBuilder.name(ACTION_DECIDED_TOPIC)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICATION_FACTOR)
                .build();
    }

    @Bean
    public NewTopic actionDecidedDlqTopic() {
        return TopicBuilder.name(ACTION_DECIDED_DLQ)
                .partitions(DEFAULT_PARTITIONS)
                .replicas(DEFAULT_REPLICATION_FACTOR)
                .build();
    }

    // =========================================================================
    // Dead Letter Queue (DLQ) & Exponential Backoff Error Handler (NFR-3.3)
    // =========================================================================

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            KafkaOperations<Object, Object> kafkaOperations) {
        return new DeadLetterPublishingRecoverer(kafkaOperations, (record, exception) -> {
            String originalTopic = record.topic();
            String dlqTopic = originalTopic + ".DLQ";
            log.error("Kafka message processing failed after retries. Routing to DLQ topic: {} (Key: {}, Offset: {}, Error: {})",
                    dlqTopic, record.key(), record.offset(), exception.getMessage());
            return new TopicPartition(dlqTopic, record.partition());
        });
    }

    @Bean
    public CommonErrorHandler commonErrorHandler(DeadLetterPublishingRecoverer recoverer) {
        // Exponential backoff: Initial interval 1.0s, Multiplier 2.0, Max interval 10.0s, Max 3 retry attempts
        ExponentialBackOff backOff = new ExponentialBackOff(1000L, 2.0);
        backOff.setMaxInterval(10000L);
        backOff.setMaxElapsedTime(30000L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                NullPointerException.class
        );
        return errorHandler;
    }
}
