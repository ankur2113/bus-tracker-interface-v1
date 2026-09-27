package com.reset.bus_tracker_interface.checkin.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CheckInRequest(
        @NotNull
        UUID tripStopId
) {
}
