package com.meetingops.domain.model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port interface for meeting persistence in the domain layer.
 *
 * <p>Defines the contract that infrastructure implementations must fulfill
 * for meeting CRUD operations and tenant-scoped queries.</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface MeetingRepository {

    /**
     * Saves a meeting record.
     *
     * @param meeting the meeting to save
     * @return the persisted meeting
     */
    Meeting save(Meeting meeting);

    /**
     * Finds a meeting by its unique identifier.
     *
     * @param id the meeting identifier
     * @return the meeting if found
     */
    Optional<Meeting> findById(UUID id);

    /**
     * Finds all meetings for a given organization, enforcing tenant isolation.
     *
     * @param organizationId the organization identifier
     * @return list of meetings for the organization
     */
    List<Meeting> findByOrganizationId(UUID organizationId);

    /**
     * Finds all meetings with a specific processing status for a given organization.
     *
     * @param organizationId the organization identifier
     * @param status         the processing status to filter by
     * @return list of matching meetings
     */
    List<Meeting> findByOrganizationIdAndStatus(UUID organizationId,
                                                com.meetingops.domain.enumeration.MeetingStatus status);
}
