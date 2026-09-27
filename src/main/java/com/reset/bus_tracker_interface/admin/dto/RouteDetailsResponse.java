package com.reset.bus_tracker_interface.admin.dto;

import java.util.List;

public record RouteDetailsResponse(
        RouteResponse route,
        List<RouteStopResponse> stops
) {
}