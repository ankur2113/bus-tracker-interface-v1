package com.reset.bus_tracker_interface.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("trip_current_location")
public record TripCurrentLocation(@Id
                                  @Column("trip_id")
                                  UUID tripId,

                                  BigDecimal latitude,
                                  BigDecimal longitude,

                                  @Column("accuracy_meters") BigDecimal accuracyMeters,
                                  @Column("speed_mps") BigDecimal speedMps,
                                  @Column("heading_degrees") BigDecimal headingDegrees,

                                  @Column("location_recorded_at") Instant locationRecordedAt,
                                  @Column("location_received_at") Instant locationReceivedAt,

                                  @ReadOnlyProperty
                                  @Column("updated_at")
                                  Instant updatedAt) {
}
