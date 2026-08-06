package com.meetingops.infrastructure.jpa.repository;

import com.meetingops.domain.enumeration.MeetingStatus;
import com.meetingops.infrastructure.jpa.entity.MeetingEntity;
import com.meetingops.infrastructure.jpa.projection.MeetingSummaryProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for meeting entities.
 * Supports tenant isolation, soft deletes, and cursor (keyset) pagination.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface JpaMeetingRepository extends JpaRepository<MeetingEntity, UUID> {

    /**
     * Finds all active meetings for a given organization.
     */
    List<MeetingEntity> findByOrganizationId(UUID organizationId);

    /**
     * Finds active meetings by organization and status.
     */
    List<MeetingEntity> findByOrganizationIdAndStatus(UUID organizationId, MeetingStatus status);

    /**
     * Memory-efficient Keyset (Cursor) Pagination query.
     * Prevents deep-offset memory bloat by filtering directly on the index key.
     */
    @Query("""
        SELECT m.id AS id,
               m.organizationId AS organizationId,
               m.teamId AS teamId,
               m.title AS title,
               m.meetingDate AS meetingDate,
               m.status AS status,
               m.createdAt AS createdAt
        FROM MeetingEntity m
        WHERE m.organizationId = :orgId
          AND (:lastCreatedAt IS NULL OR m.createdAt < :lastCreatedAt)
        ORDER BY m.createdAt DESC
        """)
    Slice<MeetingSummaryProjection> findSummaryByKeyset(
            @Param("orgId") UUID orgId,
            @Param("lastCreatedAt") Instant lastCreatedAt,
            Pageable pageable
    );
}
