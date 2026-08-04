package com.meetingops.infrastructure.jpa.repository;

import com.meetingops.infrastructure.jpa.entity.AgentTraceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for agent trace entities.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
public interface JpaAgentTraceRepository extends JpaRepository<AgentTraceEntity, UUID> {

    /**
     * Finds all trace entries for a given meeting, ordered by timestamp.
     *
     * @param meetingId the meeting identifier
     * @return list of agent trace entities
     */
    List<AgentTraceEntity> findByMeetingIdOrderByTimestampAsc(UUID meetingId);
}
