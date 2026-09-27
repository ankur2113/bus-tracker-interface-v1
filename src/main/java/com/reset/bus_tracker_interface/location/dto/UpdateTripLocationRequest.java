package com.reset.bus_tracker_interface.location.dto;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record UpdateTripLocationRequest(

        @NotNull
        @DecimalMin("-90.0")
        @DecimalMax("90.0")
        BigDecimal latitude,

        @NotNull
        @DecimalMin("-180.0")
        @DecimalMax("180.0")
        BigDecimal longitude,

        @DecimalMin("0.0")
        BigDecimal accuracyMeters,

        @DecimalMin("0.0")
        BigDecimal speedMps,

        @DecimalMin("0.0")
        @DecimalMax("360.0")
        BigDecimal headingDegrees,

        Instant locationRecordedAt
) {
}