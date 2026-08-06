package com.meetingops.infrastructure.jpa.entity;

import com.meetingops.domain.enumeration.MeetingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity for the meetings table.
 * Includes soft-delete support and optimistic locking.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Entity
@Table(name = "meetings", indexes = {
        @Index(name = "idx_meetings_org", columnList = "organization_id"),
        @Index(name = "idx_meetings_status", columnList = "status"),
        @Index(name = "idx_meetings_org_status", columnList = "organization_id, status")
})
@SQLRestriction("deleted_at IS NULL")
@DynamicUpdate
@DynamicInsert
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeetingEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "organization_id", nullable = false, columnDefinition = "uuid")
    private UUID organizationId;

    @Column(name = "team_id", columnDefinition = "uuid")
    private UUID teamId;

    @Column(nullable = false)
    private String title;

    @Column(name = "meeting_date", nullable = false)
    private Instant meetingDate;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> attendees;

    @Column(name = "transcript_s3_uri", nullable = false, columnDefinition = "text")
    private String transcriptRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingStatus status;

    @Version
    @Column(nullable = false)
    private Integer version;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
