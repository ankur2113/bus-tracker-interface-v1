package com.reset.bus_tracker_interface.admin.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RouteStopResponse(
        UUID routeStopId,
        UUID stopId,
        String stopCode,
        String stopName,
        String landmark,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer stopSequence,
        Integer plannedArrivalOffsetMinutes,
        Boolean pickupEnabled
) {
}