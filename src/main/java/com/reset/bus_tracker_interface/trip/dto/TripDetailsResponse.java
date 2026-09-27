package com.reset.bus_tracker_interface.trip.dto;

import java.util.List;

public record TripDetailsResponse(
        TripResponse trip,
        List<TripStopResponse> stops
) {
}
