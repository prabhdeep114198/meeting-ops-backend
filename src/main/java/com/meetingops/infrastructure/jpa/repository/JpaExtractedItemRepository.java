package com.meetingops.infrastructure.jpa.repository;

import com.meetingops.infrastructure.jpa.entity.ExtractedItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for extracted item entities.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface JpaExtractedItemRepository extends JpaRepository<ExtractedItemEntity, UUID> {

    /**
     * Finds all extracted items for a given meeting.
     *
     * @param meetingId the meeting identifier
     * @return list of extracted item entities
     */
    List<ExtractedItemEntity> findByMeetingId(UUID meetingId);
}
