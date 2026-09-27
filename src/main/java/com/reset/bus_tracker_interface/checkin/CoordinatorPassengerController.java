package com.reset.bus_tracker_interface.checkin;

import java.util.UUID;

import com.reset.bus_tracker_interface.checkin.dto.CheckInResponse;
import com.reset.bus_tracker_interface.checkin.dto.StopPassengerManifestResponse;

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
public class CoordinatorPassengerController {

    private final CoordinatorPassengerService passengerService;

    public CoordinatorPassengerController(
            CoordinatorPassengerService passengerService
    ) {
        this.passengerService = passengerService;
    }

    @GetMapping("/{tripId}/manifest")
    public Flux<StopPassengerManifestResponse> getPassengerManifest(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tripId
    ) {
        UUID coordinatorId = UUID.fromString(jwt.getSubject());

        return passengerService.getPassengerManifest(coordinatorId, tripId);
    }

    @GetMapping("/{tripId}/stops/{tripStopId}/check-ins")
    public Flux<CheckInResponse> getCheckInsForStop(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tripId,
            @PathVariable UUID tripStopId
    ) {
        UUID coordinatorId = UUID.fromString(jwt.getSubject());

        return passengerService.getCheckInsForStop(
                coordinatorId,
                tripId,
                tripStopId
        );
    }

    @PostMapping("/{tripId}/check-ins/{checkInId}/boarded")
    public Mono<CheckInResponse> markBoarded(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tripId,
            @PathVariable UUID checkInId
    ) {
        UUID coordinatorId = UUID.fromString(jwt.getSubject());

        return passengerService.markBoarded(
                coordinatorId,
                tripId,
                checkInId
        );
    }

    @PostMapping("/{tripId}/check-ins/{checkInId}/no-show")
    public Mono<CheckInResponse> markNoShow(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tripId,
            @PathVariable UUID checkInId
    ) {
        UUID coordinatorId = UUID.fromString(jwt.getSubject());

        return passengerService.markNoShow(
                coordinatorId,
                tripId,
                checkInId
        );
    }
}