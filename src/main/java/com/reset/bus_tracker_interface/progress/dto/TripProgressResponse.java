package com.reset.bus_tracker_interface.progress.dto;

import java.time.Instant;
import java.util.UUID;

public record TripProgressResponse(
        UUID tripId,
        UUID targetTripStopId,
        UUID targetStopId,
        String targetStopName,
        Integer targetStopSequence,
        UUID currentTripStopId,
        String currentStopName,
        Integer currentStopSequence,
        Integer stopsAway,
        Long estimatedMinutesToArrival,
        String alertLevel,
        String message,
        Instant scheduledArrivalAt
) {
}
