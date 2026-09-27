package com.reset.bus_tracker_interface.trip.dto;

import java.time.Instant;
import java.util.UUID;

import com.reset.bus_tracker_interface.domain.TripStatus;

public record TripResponse(
        UUID id,

        UUID routeId,
        String routeCode,
        String routeName,

        UUID busId,
        String busRegistrationNumber,
        String busDisplayName,

        UUID coordinatorId,
        String coordinatorName,

        Instant scheduledStartAt,
        Instant scheduledEndAt,
        Instant actualStartAt,
        Instant actualEndAt,

        TripStatus status
) {
}
