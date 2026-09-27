package com.reset.bus_tracker_interface.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1")
public class ApiStatusController {

    @GetMapping("/status")
    public Mono<ResponseEntity<ApiStatusResponse>> getStatus() {

        ApiStatusResponse response = new ApiStatusResponse(
                "bus-tracker-api",
                "UP",
                Instant.now()
        );

        return Mono.just(ResponseEntity.ok(response));
    }

    public record ApiStatusResponse(
            String service,
            String status,
            Instant timestamp
    ) {
    }
}
