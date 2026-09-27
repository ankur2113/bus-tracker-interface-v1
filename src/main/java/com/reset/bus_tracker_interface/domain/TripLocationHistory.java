package com.reset.bus_tracker_interface.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("trip_location_history")
public record TripLocationHistory( @Id UUID id,
                                   @Column("trip_id") UUID tripId,
                                   BigDecimal latitude,
                                   BigDecimal longitude,
                                   @Column("accuracy_meters") BigDecimal accuracyMeters,
                                   @Column("speed_mps") BigDecimal speedMps,
                                   @Column("heading_degrees") BigDecimal headingDegrees,
                                   @Column("location_recorded_at") Instant locationRecordedAt,
                                   @Column("location_received_at") Instant locationReceivedAt,
                                   String source) {
}
