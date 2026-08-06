package com.meetingops.infrastructure.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for the users table.
 *
 * @author MeetingOps Team
 * @since 1.0.0
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_org", columnList = "organization_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_org_email", columnNames = {"organization_id", "email"})
})
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "organization_id", nullable = false, columnDefinition = "uuid")
    private UUID organizationId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String role;

    private String name;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
