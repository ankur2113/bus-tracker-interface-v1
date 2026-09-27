package com.reset.bus_tracker_interface.domain;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("trip_stop")
public record TripStop( @Id UUID id,
                        @Column("trip_id") UUID tripId,
                        @Column("stop_id") UUID stopId,
                        @Column("stop_sequence") Integer stopSequence,
                        @Column("scheduled_arrival_at") Instant scheduledArrivalAt,
                        @Column("actual_arrival_at") Instant actualArrivalAt,
                        TripStopStatus status,
                        @ReadOnlyProperty
                        @Column("created_at")
                        Instant createdAt,
                        @ReadOnlyProperty
                        @Column("updated_at")
                        Instant updatedAt,
                        @Version Long version) {
}
