package com.reset.bus_tracker_interface.trip;

import java.util.UUID;

import com.reset.bus_tracker_interface.trip.dto.TripDetailsResponse;
import com.reset.bus_tracker_interface.trip.dto.TripResponse;
import com.reset.bus_tracker_interface.trip.dto.TripStopResponse;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/coordinator/trips")
public class CoordinatorTripController {

    private final CoordinatorTripService coordinatorTripService;

    public CoordinatorTripController(
            CoordinatorTripService coordinatorTripService
    ) {
        this.coordinatorTripService = coordinatorTripService;
    }

    @GetMapping
    public Flux<TripResponse> listAssignedTrips(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID coordinatorId = UUID.fromString(jwt.getSubject());

        return coordinatorTripService.listAssignedTrips(coordinatorId);
    }

    @GetMapping("/{tripId}")
    public Mono<TripDetailsResponse> getAssignedTripDetails(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tripId
    ) {
        UUID coordinatorId = UUID.fromString(jwt.getSubject());

        return coordinatorTripService.getAssignedTripDetails(
                coordinatorId,
                tripId
        );
    }

    @PostMapping("/{tripId}/boarding")
    public Mono<TripDetailsResponse> markBoarding(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tripId
    ) {
        UUID coordinatorId = UUID.fromString(jwt.getSubject());

        return coordinatorTripService.markBoarding(coordinatorId, tripId);
    }

    @PostMapping("/{tripId}/start")
    public Mono<TripDetailsResponse> startTrip(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tripId
    ) {
        UUID coordinatorId = UUID.fromString(jwt.getSubject());

        return coordinatorTripService.startTrip(coordinatorId, tripId);
    }

    @PostMapping("/{tripId}/end")
    public Mono<TripDetailsResponse> endTrip(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tripId
    ) {
        UUID coordinatorId = UUID.fromString(jwt.getSubject());

        return coordinatorTripService.endTrip(coordinatorId, tripId);
    }

    @PostMapping("/{tripId}/stops/{tripStopId}/arrived")
    public Mono<TripStopResponse> markStopArrived(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tripId,
            @PathVariable UUID tripStopId
    ) {
        UUID coordinatorId = UUID.fromString(jwt.getSubject());

        return coordinatorTripService.markStopArrived(
                coordinatorId,
                tripId,
                tripStopId
        );
    }

    @PostMapping("/{tripId}/stops/{tripStopId}/departed")
    public Mono<TripStopResponse> markStopDeparted(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tripId,
            @PathVariable UUID tripStopId
    ) {
        UUID coordinatorId = UUID.fromString(jwt.getSubject());

        return coordinatorTripService.markStopDeparted(
                coordinatorId,
                tripId,
                tripStopId
        );
    }

    @PostMapping("/{tripId}/stops/{tripStopId}/skipped")
    public Mono<TripStopResponse> markStopSkipped(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tripId,
            @PathVariable UUID tripStopId
    ) {
        UUID coordinatorId = UUID.fromString(jwt.getSubject());

        return coordinatorTripService.markStopSkipped(
                coordinatorId,
                tripId,
                tripStopId
        );
    }
}