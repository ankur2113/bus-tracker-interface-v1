package com.reset.bus_tracker_interface.emergency.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateEmergencyReportRequest(
        @NotBlank @Size(max = 60) String emergencyType,
        @NotBlank @Size(max = 500) String message
) {
}
