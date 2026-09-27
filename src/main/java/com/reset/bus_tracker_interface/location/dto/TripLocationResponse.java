package com.reset.bus_tracker_interface.location.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TripLocationResponse(
        UUID tripId,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal accuracyMeters,
        BigDecimal speedMps,
        BigDecimal headingDegrees,
        Instant locationRecordedAt,
        Instant locationReceivedAt
) {
}
