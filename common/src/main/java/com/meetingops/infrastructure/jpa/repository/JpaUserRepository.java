package com.meetingops.infrastructure.jpa.repository;

import com.meetingops.infrastructure.jpa.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for user entities with tenant scoping.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface JpaUserRepository extends JpaRepository<UserEntity, UUID> {

    List<UserEntity> findByOrganizationId(UUID organizationId);

    Optional<UserEntity> findByOrganizationIdAndEmail(UUID organizationId, String email);
}
