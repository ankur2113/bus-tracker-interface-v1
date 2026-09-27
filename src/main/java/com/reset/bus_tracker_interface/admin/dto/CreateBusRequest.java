package com.reset.bus_tracker_interface.admin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateBusRequest(

        @NotBlank
        @Size(max = 40)
        String registrationNumber,

        @Size(max = 100)
        String displayName,

        @NotNull
        @Min(1)
        Integer capacity
) {
}