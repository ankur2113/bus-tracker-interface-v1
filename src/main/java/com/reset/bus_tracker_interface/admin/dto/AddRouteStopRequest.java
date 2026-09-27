package com.reset.bus_tracker_interface.admin.dto;

import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddRouteStopRequest(

        @NotNull
        UUID stopId,

        @NotNull
        @Min(1)
        Integer stopSequence,

        @Min(0)
        Integer plannedArrivalOffsetMinutes,

        Boolean pickupEnabled
) {
}