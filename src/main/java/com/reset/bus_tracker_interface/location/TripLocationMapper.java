package com.reset.bus_tracker_interface.location;

import com.reset.bus_tracker_interface.domain.TripCurrentLocation;
import com.reset.bus_tracker_interface.domain.TripLocationHistory;
import com.reset.bus_tracker_interface.location.dto.TripLocationHistoryResponse;
import com.reset.bus_tracker_interface.location.dto.TripLocationResponse;

public final class TripLocationMapper {

    private TripLocationMapper() {
    }

    public static TripLocationResponse toCurrentLocationResponse(
            TripCurrentLocation location
    ) {
        return new TripLocationResponse(
                location.tripId(),
                location.latitude(),
                location.longitude(),
                location.accuracyMeters(),
                location.speedMps(),
                location.headingDegrees(),
                location.locationRecordedAt(),
                location.locationReceivedAt()
        );
    }

    public static TripLocationHistoryResponse toHistoryResponse(
            TripLocationHistory location
    ) {
        return new TripLocationHistoryResponse(
                location.id(),
                location.tripId(),
                location.latitude(),
                location.longitude(),
                location.accuracyMeters(),
                location.speedMps(),
                location.headingDegrees(),
                location.locationRecordedAt(),
                location.locationReceivedAt(),
                location.source()
        );
    }
}