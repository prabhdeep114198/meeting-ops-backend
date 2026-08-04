package com.meetingops.infrastructure.repository;

import com.meetingops.domain.enumeration.MeetingStatus;
import com.meetingops.domain.model.Meeting;
import com.meetingops.domain.model.MeetingRepository;
import com.meetingops.infrastructure.jpa.entity.MeetingEntity;
import com.meetingops.infrastructure.jpa.repository.JpaMeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JPA-based implementation of the {@link MeetingRepository} port.
 *
 * <p>Bridges the domain model to the JPA persistence layer. All queries
 * are scoped by organization ID to enforce tenant isolation (DATA-3).</p>
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Repository
@RequiredArgsConstructor
public class MeetingRepositoryImpl implements MeetingRepository {

    private final JpaMeetingRepository jpaRepository;

    @Override
    public Meeting save(final Meeting meeting) {
        MeetingEntity entity = toEntity(meeting);
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public Optional<Meeting> findById(final UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Meeting> findByOrganizationId(final UUID organizationId) {
        return jpaRepository.findByOrganizationId(organizationId)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Meeting> findByOrganizationIdAndStatus(final UUID organizationId,
                                                       final MeetingStatus status) {
        return jpaRepository.findByOrganizationIdAndStatus(organizationId, status)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Converts a domain Meeting to a JPA entity.
     */
    private MeetingEntity toEntity(final Meeting meeting) {
        return MeetingEntity.builder()
                .id(meeting.id())
                .organizationId(meeting.organizationId())
                .teamId(meeting.teamId())
                .title(meeting.title())
                .meetingDate(meeting.meetingDate())
                .attendees(meeting.attendees())
                .transcriptRef(meeting.transcriptRef())
                .status(meeting.status())
                .createdAt(meeting.createdAt())
                .updatedAt(meeting.updatedAt())
                .build();
    }

    /**
     * Converts a JPA entity to a domain Meeting.
     */
    private Meeting toDomain(final MeetingEntity entity) {
        return new Meeting(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getTeamId(),
                entity.getTitle(),
                entity.getMeetingDate(),
                entity.getAttendees(),
                entity.getTranscriptRef(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
