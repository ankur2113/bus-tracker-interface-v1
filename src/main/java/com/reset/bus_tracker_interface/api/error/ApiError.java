package com.reset.bus_tracker_interface.api.error;

import java.time.Instant;

public record ApiError(Instant timestamp,
                       int status,
                       String code,
                       String message,
                       String path) {
}
