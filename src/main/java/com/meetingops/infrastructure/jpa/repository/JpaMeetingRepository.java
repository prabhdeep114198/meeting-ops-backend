package com.meetingops.infrastructure.jpa.repository;

import com.meetingops.domain.enumeration.MeetingStatus;
import com.meetingops.infrastructure.jpa.entity.MeetingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for meeting entities.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface JpaMeetingRepository extends JpaRepository<MeetingEntity, UUID> {

    /**
     * Finds all meetings for a given organization (tenant isolation).
     *
     * @param organizationId the organization identifier
     * @return list of meeting entities
     */
    List<MeetingEntity> findByOrganizationId(UUID organizationId);

    /**
     * Finds meetings by organization and status.
     *
     * @param organizationId the organization identifier
     * @param status         the meeting status
     * @return list of matching meeting entities
     */
    List<MeetingEntity> findByOrganizationIdAndStatus(UUID organizationId, MeetingStatus status);
}
