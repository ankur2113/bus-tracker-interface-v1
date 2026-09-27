package com.reset.bus_tracker_interface.assignment.dto;

import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;

import java.time.Instant;
import java.util.UUID;

public record CommuterRouteAssignmentResponse(
        UUID id,
        UUID commuterId,
        UUID routeId,
        UUID defaultStopId,
        Boolean active,

        @Column("created_at")
        Instant createdAt,

        @Column("updated_at")
        Instant updatedAt
) {
}