package com.reset.bus_tracker_interface.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("bus")
public record Bus( @Id UUID id,
                   @Column("registration_number") String registrationNumber,
                   @Column("display_name") String displayName,
                   Integer capacity,
                   Boolean active,
                   @ReadOnlyProperty
                   @Column("created_at")
                   Instant createdAt,
                   @ReadOnlyProperty
                   @Column("updated_at")
                   Instant updatedAt,
                   @Version Long version) {
}
