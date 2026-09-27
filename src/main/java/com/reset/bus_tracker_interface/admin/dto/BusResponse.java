package com.reset.bus_tracker_interface.admin.dto;

import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;

import java.time.Instant;
import java.util.UUID;

public record BusResponse(
        UUID id,
        String registrationNumber,
        String displayName,
        Integer capacity,
        Boolean active,

        @Column("created_at")
        Instant createdAt,

        @Column("updated_at")
        Instant updatedAt
) {
}