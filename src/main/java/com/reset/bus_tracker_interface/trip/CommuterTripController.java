package com.reset.bus_tracker_interface.trip;

import java.util.UUID;

import com.reset.bus_tracker_interface.trip.dto.TripDetailsResponse;
import com.reset.bus_tracker_interface.trip.dto.TripResponse;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/commuter/trips")
public class CommuterTripController {

    private final CommuterTripService commuterTripService;

    public CommuterTripController(CommuterTripService commuterTripService) {
        this.commuterTripService = commuterTripService;
    }

    @GetMapping
    public Flux<TripResponse> listVisibleTrips(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID commuterId = UUID.fromString(jwt.getSubject());

        return commuterTripService.listVisibleTrips(commuterId);
    }

    @GetMapping("/{tripId}")
    public Mono<TripDetailsResponse> getVisibleTripDetails(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tripId
    ) {
        UUID commuterId = UUID.fromString(jwt.getSubject());

        return commuterTripService.getVisibleTripDetails(commuterId, tripId);
    }
}