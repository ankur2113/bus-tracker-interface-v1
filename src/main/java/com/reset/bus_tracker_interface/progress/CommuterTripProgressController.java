package com.reset.bus_tracker_interface.progress;

import com.reset.bus_tracker_interface.progress.dto.TripProgressResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/commuter/trips")
public class CommuterTripProgressController {
    private final TripProgressService tripProgressService;

    public CommuterTripProgressController(TripProgressService tripProgressService) {
        this.tripProgressService = tripProgressService;
    }

    @GetMapping("/{tripId}/progress")
    public Mono<TripProgressResponse> getProgress(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tripId
    ) {
        UUID commuterId = UUID.fromString(jwt.getSubject());
        return tripProgressService.getProgress(commuterId, tripId);
    }
}
