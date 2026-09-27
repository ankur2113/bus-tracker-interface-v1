package com.reset.bus_tracker_interface.checkin.dto;

import java.util.List;
import java.util.UUID;

import com.reset.bus_tracker_interface.domain.TripStopStatus;

public record StopPassengerManifestResponse(
        UUID tripStopId,
        UUID stopId,
        String stopCode,
        String stopName,
        Integer stopSequence,
        TripStopStatus stopStatus,
        List<CheckInResponse> passengers
) {
}