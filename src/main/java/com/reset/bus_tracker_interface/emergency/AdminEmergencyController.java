package com.reset.bus_tracker_interface.emergency;

import com.reset.bus_tracker_interface.emergency.dto.EmergencyReportResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/emergencies")
public class AdminEmergencyController {
    private final EmergencyReportService emergencyReportService;

    public AdminEmergencyController(EmergencyReportService emergencyReportService) {
        this.emergencyReportService = emergencyReportService;
    }

    @GetMapping
    public Flux<EmergencyReportResponse> listOpenEmergencies() {
        return emergencyReportService.listOpenEmergencies();
    }

    @PostMapping("/{emergencyId}/resolve")
    public Mono<EmergencyReportResponse> resolveEmergency(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID emergencyId
    ) {
        UUID adminId = UUID.fromString(jwt.getSubject());
        return emergencyReportService.resolveEmergency(adminId, emergencyId);
    }
}
