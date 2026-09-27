package com.reset.bus_tracker_interface.auth.dto;

import java.util.List;

public record CurrentUserResponse(
        String userId,
        String username,
        String fullName,
        List<String> roles
) {
}
