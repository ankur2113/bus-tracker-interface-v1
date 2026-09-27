package com.reset.bus_tracker_interface.checkin.dto;

import java.time.Instant;
import java.util.UUID;

import com.reset.bus_tracker_interface.domain.CheckInStatus;

public record CheckInResponse(
        UUID id,

        UUID tripId,
        UUID tripStopId,

        UUID commuterId,
        String commuterName,
        String commuterPhone,

        UUID stopId,
        String stopCode,
        String stopName,
        Integer stopSequence,

        CheckInStatus status,


        Instant checkedInAt,
        Instant boardedAt,
        Instant cancelledAt
) {
}