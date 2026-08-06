package com.meetingops.infrastructure.jpa.repository;

import com.meetingops.infrastructure.jpa.entity.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for organization entities.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface JpaOrganizationRepository extends JpaRepository<OrganizationEntity, UUID> {

    Optional<OrganizationEntity> findByName(String name);
}
