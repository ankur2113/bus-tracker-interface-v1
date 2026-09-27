package com.reset.bus_tracker_interface.admin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        @NotBlank
        @Size(max = 150)
        String fullName,

        @Email
        @Size(max = 255)
        String email,

        @Size(max = 25)
        String phone,

        Boolean active
) {
}