package com.reset.bus_tracker_interface.emergency;

import com.reset.bus_tracker_interface.domain.EmergencyReport;
import com.reset.bus_tracker_interface.emergency.dto.EmergencyReportResponse;

public final class EmergencyReportMapper {
    private EmergencyReportMapper() {
    }

    public static EmergencyReportResponse toResponse(EmergencyReport report) {
        return new EmergencyReportResponse(
                report.id(),
                report.tripId(),
                report.reportedByUserId(),
                report.emergencyType(),
                report.message(),
                report.status(),
                report.reportedAt(),
                report.resolvedAt(),
                report.resolvedByUserId()
        );
    }
}
