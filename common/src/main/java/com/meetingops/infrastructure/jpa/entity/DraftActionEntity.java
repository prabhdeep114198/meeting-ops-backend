package com.meetingops.infrastructure.jpa.entity;

import com.meetingops.domain.enumeration.DraftActionStatus;
import com.meetingops.domain.enumeration.DraftActionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the draft_actions table.
 * Features optimistic locking for concurrent review protection.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Entity
@Table(name = "draft_actions", indexes = {
        @Index(name = "idx_draft_actions_meeting", columnList = "meeting_id"),
        @Index(name = "idx_draft_actions_status", columnList = "status"),
        @Index(name = "idx_draft_actions_item", columnList = "extracted_item_id")
})
@DynamicUpdate
@DynamicInsert
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DraftActionEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "extracted_item_id", nullable = false, columnDefinition = "uuid")
    private UUID extractedItemId;

    @Column(name = "meeting_id", nullable = false, columnDefinition = "uuid")
    private UUID meetingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private DraftActionType actionType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", nullable = false, columnDefinition = "jsonb")
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DraftActionStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "original_payload", nullable = false, columnDefinition = "jsonb")
    private String originalPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "final_payload", columnDefinition = "jsonb")
    private String finalPayload;

    @Column(name = "prompt_version")
    private String promptVersion;

    @Version
    @Column(nullable = false)
    private Integer version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
