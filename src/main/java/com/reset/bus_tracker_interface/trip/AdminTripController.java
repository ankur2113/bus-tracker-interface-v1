package com.reset.bus_tracker_interface.trip;

import java.net.URI;
import java.util.UUID;

import com.reset.bus_tracker_interface.trip.dto.CreateTripRequest;
import com.reset.bus_tracker_interface.trip.dto.TripDetailsResponse;
import com.reset.bus_tracker_interface.trip.dto.TripResponse;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/admin/trips")
public class AdminTripController {

    private final AdminTripService adminTripService;

    public AdminTripController(AdminTripService adminTripService) {
        this.adminTripService = adminTripService;
    }

    @GetMapping
    public Flux<TripResponse> listTrips() {
        return adminTripService.listTrips();
    }

    @GetMapping("/{tripId}")
    public Mono<TripDetailsResponse> getTripDetails(
            @PathVariable UUID tripId
    ) {
        return adminTripService.getTripDetails(tripId);
    }

    @PostMapping
    public Mono<ResponseEntity<TripDetailsResponse>> createTrip(
            @Valid @RequestBody CreateTripRequest request
    ) {
        return adminTripService.createTrip(request)
                .map(response -> ResponseEntity
                        .created(URI.create(
                                "/api/v1/admin/trips/"
                                        + response.trip().id()
                        ))
                        .body(response));
    }

    @PostMapping("/{tripId}/cancel")
    public Mono<TripDetailsResponse> cancelTrip(
            @PathVariable UUID tripId
    ) {
        return adminTripService.cancelTrip(tripId);
    }
}
