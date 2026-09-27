package com.reset.bus_tracker_interface.trip.dto;

import java.time.Instant;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateTripRequest(

        @NotNull
        UUID routeId,

        @NotNull
        UUID busId,

        @NotNull
        UUID coordinatorId,

        @NotNull
        Instant scheduledStartAt,

        Instant scheduledEndAt
) {
}