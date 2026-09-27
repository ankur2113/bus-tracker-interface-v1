package com.reset.bus_tracker_interface.checkin;

import java.util.UUID;

import com.reset.bus_tracker_interface.checkin.dto.CheckInRequest;
import com.reset.bus_tracker_interface.checkin.dto.CheckInResponse;

import jakarta.validation.Valid;

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
@RequestMapping("/api/v1/commuter")
public class CommuterCheckInController {

    private final CommuterCheckInService commuterCheckInService;

    public CommuterCheckInController(
            CommuterCheckInService commuterCheckInService
    ) {
        this.commuterCheckInService = commuterCheckInService;
    }

    @GetMapping("/check-in")
    public Flux<CheckInResponse> listMyCheckIns(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID commuterId = UUID.fromString(jwt.getSubject());

        return commuterCheckInService.listMyCheckIns(commuterId);
    }

    @PostMapping("/trips/{tripId}/check-in")
    public Mono<CheckInResponse> checkIn(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tripId,
            @Valid @RequestBody CheckInRequest request
    ) {
        UUID commuterId = UUID.fromString(jwt.getSubject());

        return commuterCheckInService.checkIn(
                commuterId,
                tripId,
                request
        );
    }

    @PostMapping("/trips/{tripId}/check-in/cancel")
    public Mono<CheckInResponse> cancelCheckIn(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tripId
    ) {
        UUID commuterId = UUID.fromString(jwt.getSubject());

        return commuterCheckInService.cancelCheckIn(commuterId, tripId);
    }
}