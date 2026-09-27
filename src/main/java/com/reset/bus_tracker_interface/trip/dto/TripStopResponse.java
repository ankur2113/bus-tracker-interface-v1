package com.reset.bus_tracker_interface.trip.dto;

import com.reset.bus_tracker_interface.domain.TripStopStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TripStopResponse(
        UUID id,
        UUID stopId,

        String stopCode,
        String stopName,
        String landmark,

        BigDecimal latitude,
        BigDecimal longitude,

        Integer stopSequence,

        Instant scheduledArrivalAt,
        Instant actualArrivalAt,

        TripStopStatus status
) {
}