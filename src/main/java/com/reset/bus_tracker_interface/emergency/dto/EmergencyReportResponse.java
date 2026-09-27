package com.reset.bus_tracker_interface.emergency.dto;

import com.reset.bus_tracker_interface.domain.EmergencyStatus;

import java.time.Instant;
import java.util.UUID;

public record EmergencyReportResponse(
        UUID id,
        UUID tripId,
        UUID reportedByUserId,
        String emergencyType,
        String message,
        EmergencyStatus status,
        Instant reportedAt,
        Instant resolvedAt,
        UUID resolvedByUserId
) {
}
