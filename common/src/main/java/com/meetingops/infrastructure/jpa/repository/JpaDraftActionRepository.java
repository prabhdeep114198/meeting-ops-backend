package com.meetingops.infrastructure.jpa.repository;

import com.meetingops.domain.enumeration.DraftActionStatus;
import com.meetingops.infrastructure.jpa.entity.DraftActionEntity;
import com.meetingops.infrastructure.jpa.projection.DraftActionSummaryProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for draft action entities.
 * Optimized for human review queue reads and partial index traversal.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface JpaDraftActionRepository extends JpaRepository<DraftActionEntity, UUID> {

    /**
     * Finds all draft actions for a given meeting.
     */
    List<DraftActionEntity> findByMeetingId(UUID meetingId);

    /**
     * Finds draft actions with DRAFTED status for a given organization.
     */
    List<DraftActionEntity> findByStatus(DraftActionStatus status);

    /**
     * Query targeting partial index idx_draft_actions_pending_review.
     * Lightweight projection streaming for human-in-the-loop review queue.
     */
    @Query("""
        SELECT d.id AS id,
               d.extractedItemId AS extractedItemId,
               d.meetingId AS meetingId,
               d.actionType AS actionType,
               d.status AS status,
               d.promptVersion AS promptVersion,
               d.version AS version,
               d.createdAt AS createdAt
        FROM DraftActionEntity d
        WHERE d.status = 'DRAFTED'
        ORDER BY d.createdAt DESC
        """)
    Slice<DraftActionSummaryProjection> findPendingReviewQueue(Pageable pageable);
}
