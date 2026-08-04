package com.meetingops.infrastructure.jpa.repository;

import com.meetingops.domain.enumeration.DraftActionStatus;
import com.meetingops.infrastructure.jpa.entity.DraftActionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for draft action entities.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface JpaDraftActionRepository extends JpaRepository<DraftActionEntity, UUID> {

    /**
     * Finds all draft actions for a given meeting.
     *
     * @param meetingId the meeting identifier
     * @return list of draft action entities
     */
    List<DraftActionEntity> findByMeetingId(UUID meetingId);

    /**
     * Finds draft actions with DRAFTED status for a given organization.
     * This powers the review queue with tenant isolation.
     *
     * @param status the draft action status
     * @return list of pending draft action entities
     */
    List<DraftActionEntity> findByStatus(DraftActionStatus status);
}
