package com.reset.bus_tracker_interface.assignment.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record AssignCommuterRouteRequest(

        @NotNull
        UUID commuterId,

        @NotNull
        UUID routeId,

        UUID defaultStopId
) {
}
