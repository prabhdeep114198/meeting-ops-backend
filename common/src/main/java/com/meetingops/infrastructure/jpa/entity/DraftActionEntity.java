package com.meetingops.infrastructure.jpa.entity;

import com.meetingops.domain.enumeration.DraftActionStatus;
import com.meetingops.domain.enumeration.DraftActionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the draft_actions table.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Entity
@Table(name = "draft_actions")
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

    @Column(name = "payload_json", nullable = false, columnDefinition = "text")
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DraftActionStatus status;

    @Column(name = "original_payload", nullable = false, columnDefinition = "text")
    private String originalPayload;

    @Column(name = "final_payload", columnDefinition = "text")
    private String finalPayload;

    @Column(name = "prompt_version")
    private String promptVersion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
