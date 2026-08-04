package com.meetingops.domain.model;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port interface for user persistence in the domain layer.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface UserRepository {

    /**
     * Saves a user.
     *
     * @param user the user to save
     * @return the persisted user
     */
    User save(User user);

    /**
     * Finds a user by their unique identifier.
     *
     * @param id the user identifier
     * @return the user if found
     */
    Optional<User> findById(UUID id);

    /**
     * Finds a user by email within a specific organization.
     *
     * @param organizationId the organization identifier
     * @param email          the user's email address
     * @return the user if found
     */
    Optional<User> findByOrganizationIdAndEmail(UUID organizationId, String email);

    /**
     * Finds all users for a given organization.
     *
     * @param organizationId the organization identifier
     * @return list of users in the organization
     */
    List<User> findByOrganizationId(UUID organizationId);
}
