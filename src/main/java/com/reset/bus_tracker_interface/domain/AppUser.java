package com.reset.bus_tracker_interface.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("app_user")
public record AppUser( @Id UUID id,
                       String username,
                       @Column("full_name") String fullName,
                       String email,
                       String phone,
                       UserRole role,
                       @Column("password_hash")
                       String passwordHash,
                       Boolean active,
                       @ReadOnlyProperty
                       @Column("created_at")
                       Instant createdAt,
                       @ReadOnlyProperty
                       @Column("updated_at")
                       Instant updatedAt,
                       @Version Long version) {
}
