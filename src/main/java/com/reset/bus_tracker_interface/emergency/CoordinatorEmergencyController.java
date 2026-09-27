package com.reset.bus_tracker_interface.emergency;

import com.reset.bus_tracker_interface.emergency.dto.CreateEmergencyReportRequest;
import com.reset.bus_tracker_interface.emergency.dto.EmergencyReportResponse;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/coordinator/trips/{tripId}/emergencies")
public class CoordinatorEmergencyController {
    private final EmergencyReportService emergencyReportService;

    public CoordinatorEmergencyController(EmergencyReportService emergencyReportService) {
        this.emergencyReportService = emergencyReportService;
    }

    @PostMapping
    public Mono<EmergencyReportResponse> reportEmergency(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tripId,
            @Valid @RequestBody CreateEmergencyReportRequest request
    ) {
        UUID coordinatorId = UUID.fromString(jwt.getSubject());
        return emergencyReportService.reportEmergency(coordinatorId, tripId, request);
    }

    @GetMapping
    public Flux<EmergencyReportResponse> listTripEmergencies(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID tripId
    ) {
        UUID coordinatorId = UUID.fromString(jwt.getSubject());
        return emergencyReportService.listTripEmergencies(coordinatorId, tripId);
    }
}
