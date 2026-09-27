package com.reset.bus_tracker_interface.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("commuter_check_in")
public record CommuterCheckIn( @Id UUID id,
                               @Column("trip_id") UUID tripId,
                               @Column("trip_stop_id") UUID tripStopId,
                               @Column("commuter_id") UUID commuterId,
                               CheckInStatus status,
                               @Column("checked_in_at") Instant checkedInAt,
                               @Column("boarded_at") Instant boardedAt,
                               @Column("cancelled_at") Instant cancelledAt,
                               @ReadOnlyProperty
                               @Column("created_at")
                               Instant createdAt,
                               @ReadOnlyProperty
                               @Column("updated_at")
                               Instant updatedAt,
                               @Version Long version) {
}
