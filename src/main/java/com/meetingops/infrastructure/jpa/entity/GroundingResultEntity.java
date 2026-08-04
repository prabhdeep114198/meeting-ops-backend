package com.meetingops.infrastructure.jpa.entity;

import com.meetingops.domain.enumeration.GroundingClassification;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the grounding_results table.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Entity
@Table(name = "grounding_results")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroundingResultEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "extracted_item_id", nullable = false, columnDefinition = "uuid")
    private UUID extractedItemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroundingClassification classification;

    @Column(name = "cited_item_id", columnDefinition = "uuid")
    private UUID citedItemId;

    @Column(name = "cited_meeting_id", columnDefinition = "uuid")
    private UUID citedMeetingId;

    @Column(nullable = false, columnDefinition = "text")
    private String rationale;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
