package com.meetingops.infrastructure.jpa.entity;

import com.meetingops.domain.enumeration.ItemStatus;
import com.meetingops.domain.enumeration.ItemType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the extracted_items table.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Entity
@Table(name = "extracted_items")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedItemEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "meeting_id", nullable = false, columnDefinition = "uuid")
    private UUID meetingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType type;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    private String owner;

    private String deadline;

    @Column(name = "supporting_excerpt", nullable = false, columnDefinition = "text")
    private String supportingExcerpt;

    @Column(nullable = false)
    private Double confidence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemStatus status;

    @Column(name = "prompt_version")
    private String promptVersion;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
