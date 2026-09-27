package com.reset.bus_tracker_interface.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRouteRequest(

        @NotBlank
        @Size(max = 40)
        String routeCode,

        @NotBlank
        @Size(max = 150)
        String routeName,

        @Size(max = 500)
        String description
) {
}