package com.meetingops.infrastructure.jpa.entity;

import com.meetingops.domain.enumeration.AuditAction;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the review_decisions table.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Entity
@Table(name = "review_decisions")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDecisionEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "draft_action_id", nullable = false, columnDefinition = "uuid")
    private UUID draftActionId;

    @Column(name = "reviewer_id", nullable = false, columnDefinition = "uuid")
    private UUID reviewerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Column(name = "original_payload", nullable = false, columnDefinition = "text")
    private String originalPayload;

    @Column(name = "final_payload", columnDefinition = "text")
    private String finalPayload;

    @Column(nullable = false, columnDefinition = "text")
    private String reason;

    @Column(nullable = false)
    private Instant timestamp;
}
