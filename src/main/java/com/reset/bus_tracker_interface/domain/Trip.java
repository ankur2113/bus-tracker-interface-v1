package com.reset.bus_tracker_interface.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("trip")
public record Trip(  @Id UUID id,
                     @Column("route_id") UUID routeId,
                     @Column("bus_id") UUID busId,
                     @Column("coordinator_id") UUID coordinatorId,
                     @Column("scheduled_start_at") Instant scheduledStartAt,
                     @Column("scheduled_end_at") Instant scheduledEndAt,
                     @Column("actual_start_at") Instant actualStartAt,
                     @Column("actual_end_at") Instant actualEndAt,
                     TripStatus status,
                     @ReadOnlyProperty
                     @Column("created_at")
                     Instant createdAt,
                     @ReadOnlyProperty
                     @Column("updated_at")
                     Instant updatedAt,
                     @Version Long version) {
}
