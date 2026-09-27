package com.reset.bus_tracker_interface.auth.dto;

import com.reset.bus_tracker_interface.domain.UserRole;

import java.util.UUID;

public record AuthenticatedUserResponse(
        UUID id,
        String username,
        String fullName,
        UserRole role
) {
}
