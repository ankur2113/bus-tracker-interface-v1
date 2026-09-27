package com.reset.bus_tracker_interface.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("emergency_report")
public record EmergencyReport(
        @Id UUID id,
        @Column("trip_id") UUID tripId,
        @Column("reported_by_user_id") UUID reportedByUserId,
        @Column("emergency_type") String emergencyType,
        String message,
        EmergencyStatus status,
        @Column("reported_at") Instant reportedAt,
        @Column("resolved_at") Instant resolvedAt,
        @Column("resolved_by_user_id") UUID resolvedByUserId,
        @ReadOnlyProperty @Column("created_at") Instant createdAt,
        @ReadOnlyProperty @Column("updated_at") Instant updatedAt,
        @Version Long version
) {
}
