package com.reset.bus_tracker_interface.admin.dto;

import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record StopResponse(
        UUID id,
        String stopCode,
        String stopName,
        String landmark,
        BigDecimal latitude,
        BigDecimal longitude,
        Boolean active,

        @Column("created_at")
        Instant createdAt,

        @Column("updated_at")
        Instant updatedAt
) {
}