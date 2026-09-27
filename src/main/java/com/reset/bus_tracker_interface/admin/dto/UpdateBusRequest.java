package com.reset.bus_tracker_interface.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateBusRequest(

        @Size(max = 100)
        String displayName,

        @Min(1)
        Integer capacity,

        Boolean active
) {
}