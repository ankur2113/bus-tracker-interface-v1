package com.reset.bus_tracker_interface.domain;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("route_stop")
public record RouteStop( @Id UUID id,
                         @Column("route_id") UUID routeId,
                         @Column("stop_id") UUID stopId,
                         @Column("stop_sequence") Integer stopSequence,
                         @Column("planned_arrival_offset_minutes") Integer plannedArrivalOffsetMinutes,
                         @Column("pickup_enabled") Boolean pickupEnabled,

                         @ReadOnlyProperty
                         @Column("created_at")
                         Instant createdAt,

                         @ReadOnlyProperty
                         @Column("updated_at")
                         Instant updatedAt,
                         @Version Long version) {
}
