package com.reset.bus_tracker_interface.location.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TripLocationHistoryResponse(
        UUID id,
        UUID tripId,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal accuracyMeters,
        BigDecimal speedMps,
        BigDecimal headingDegrees,
        Instant locationRecordedAt,
        Instant locationReceivedAt,
        String source
) {
}