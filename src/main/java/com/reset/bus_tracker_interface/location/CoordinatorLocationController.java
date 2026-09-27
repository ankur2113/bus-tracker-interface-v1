package com.reset.bus_tracker_interface.location;

import java.time.Duration;
import java.util.UUID;

import com.reset.bus_tracker_interface.location.dto.TripLocationHistoryResponse;
import com.reset.bus_tracker_interface.location.dto.TripLocationResponse;
import com.reset.bus_tracker_interface.location.dto.UpdateTripLocationRequest;

import jakarta.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/coordinator/trips")
public class CoordinatorLocationController {

    private final TripLocationService tripLocationService;

    public CoordinatorLocationController(
            TripLocationService tripLocationService
    ) {
        this.tripLocationService = tripLocationService;
    }

    @PostMapping("/{tripId}/location")
    public Mono<TripLocationResponse> updateLocation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tripId,
            @Valid @RequestBody UpdateTripLocationRequest request
    ) {
        UUID coordinatorId = UUID.fromString(jwt.getSubject());

        return tripLocationService.updateCoordinatorLocation(
                coordinatorId,
                tripId,
                request
        );
    }

    @GetMapping("/{tripId}/location")
    public Mono<TripLocationResponse> getLatestLocation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tripId
    ) {
        UUID coordinatorId = UUID.fromString(jwt.getSubject());

        return tripLocationService.getLatestLocationForCoordinator(
                coordinatorId,
                tripId
        );
    }

    @GetMapping("/{tripId}/location/history")
    public Flux<TripLocationHistoryResponse> getLocationHistory(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tripId
    ) {
        UUID coordinatorId = UUID.fromString(jwt.getSubject());

        return tripLocationService.getLocationHistoryForCoordinator(
                coordinatorId,
                tripId
        );
    }

    @GetMapping(
            value = "/{tripId}/location/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public Flux<ServerSentEvent<TripLocationResponse>> streamLocation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tripId
    ) {
        UUID coordinatorId = UUID.fromString(jwt.getSubject());

        Flux<ServerSentEvent<TripLocationResponse>> locationEvents =
                tripLocationService
                        .streamLocationForCoordinator(coordinatorId, tripId)
                        .map(location -> ServerSentEvent
                                .<TripLocationResponse>builder(location)
                                .event("location")
                                .id(location.locationRecordedAt().toString())
                                .build()
                        );

        Flux<ServerSentEvent<TripLocationResponse>> heartbeatEvents =
                Flux.interval(Duration.ofSeconds(15))
                        .map(sequence -> ServerSentEvent
                                .<TripLocationResponse>builder()
                                .event("heartbeat")
                                .comment("keep-alive")
                                .build()
                        );

        return Flux.merge(locationEvents, heartbeatEvents);
    }
}
