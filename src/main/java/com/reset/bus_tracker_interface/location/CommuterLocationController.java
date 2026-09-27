package com.reset.bus_tracker_interface.location;

import java.time.Duration;
import java.util.UUID;

import com.reset.bus_tracker_interface.location.dto.TripLocationResponse;

import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
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
public class CommuterLocationController {

    private final TripLocationService tripLocationService;

    public CommuterLocationController(
            TripLocationService tripLocationService
    ) {
        this.tripLocationService = tripLocationService;
    }

    @GetMapping("/{tripId}/location")
    public Mono<TripLocationResponse> getLatestLocation(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tripId
    ) {
        UUID commuterId = UUID.fromString(jwt.getSubject());

        return tripLocationService.getLatestLocationForCommuter(
                commuterId,
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
        UUID commuterId = UUID.fromString(jwt.getSubject());

        Flux<ServerSentEvent<TripLocationResponse>> locationEvents =
                tripLocationService
                        .streamLocationForCommuter(commuterId, tripId)
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