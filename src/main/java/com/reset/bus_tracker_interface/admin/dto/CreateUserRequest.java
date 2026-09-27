package com.reset.bus_tracker_interface.admin.dto;

import com.reset.bus_tracker_interface.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

        @NotBlank
        @Size(max = 60)
        String username,

        @NotBlank
        @Size(max = 150)
        String fullName,

        @Email
        @Size(max = 255)
        String email,

        @Size(max = 25)
        String phone,

        @NotNull
        UserRole role,

        @NotBlank
        @Size(min = 8, max = 128)
        String password
) {
}