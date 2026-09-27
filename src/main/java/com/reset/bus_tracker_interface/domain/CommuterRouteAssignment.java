package com.reset.bus_tracker_interface.domain;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("commuter_route_assignment")
public record CommuterRouteAssignment(
        @Id UUID id,
        @Column("commuter_id") UUID commuterId,
        @Column("route_id") UUID routeId,
        @Column("default_stop_id") UUID defaultStopId,
        Boolean active,
        @ReadOnlyProperty
        @Column("created_at")
        Instant createdAt,
        @ReadOnlyProperty
        @Column("updated_at")
        Instant updatedAt,
        @Version Long version
) {
}