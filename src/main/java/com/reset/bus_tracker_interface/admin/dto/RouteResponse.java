package com.reset.bus_tracker_interface.admin.dto;

import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;

import java.time.Instant;
import java.util.UUID;

public record RouteResponse(
        UUID id,
        String routeCode,
        String routeName,
        String description,
        Boolean active,

        @Column("created_at")
        Instant createdAt,

        @Column("updated_at")
        Instant updatedAt
) {
}