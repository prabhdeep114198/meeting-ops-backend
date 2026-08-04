package com.meetingops.domain.model;

import java.util.Optional;
import java.util.UUID;

/**
 * Port interface for organization persistence in the domain layer.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface OrganizationRepository {

    /**
     * Saves an organization.
     *
     * @param organization the organization to save
     * @return the persisted organization
     */
    Organization save(Organization organization);

    /**
     * Finds an organization by its unique identifier.
     *
     * @param id the organization identifier
     * @return the organization if found
     */
    Optional<Organization> findById(UUID id);

    /**
     * Finds an organization by its name.
     *
     * @param name the organization name
     * @return the organization if found
     */
    Optional<Organization> findByName(String name);
}
