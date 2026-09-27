package com.reset.bus_tracker_interface.admin.controller;

import java.net.URI;
import java.util.UUID;

import com.reset.bus_tracker_interface.admin.dto.CreateStopRequest;
import com.reset.bus_tracker_interface.admin.dto.StopResponse;
import com.reset.bus_tracker_interface.admin.dto.UpdateStopRequest;

import com.reset.bus_tracker_interface.admin.service.AdminStopService;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/admin/stops")
public class AdminStopController {

    private final AdminStopService adminStopService;

    public AdminStopController(AdminStopService adminStopService) {
        this.adminStopService = adminStopService;
    }

    @GetMapping
    public Flux<StopResponse> listStops() {
        return adminStopService.listStops();
    }

    @GetMapping("/{stopId}")
    public Mono<StopResponse> getStop(@PathVariable UUID stopId) {
        return adminStopService.getStop(stopId);
    }

    @PostMapping
    public Mono<ResponseEntity<StopResponse>> createStop(
            @Valid @RequestBody CreateStopRequest request
    ) {
        return adminStopService.createStop(request)
                .map(response -> ResponseEntity
                        .created(URI.create(
                                "/api/v1/admin/stops/" + response.id()
                        ))
                        .body(response));
    }

    @PutMapping("/{stopId}")
    public Mono<StopResponse> updateStop(
            @PathVariable UUID stopId,
            @Valid @RequestBody UpdateStopRequest request
    ) {
        return adminStopService.updateStop(stopId, request);
    }

    @DeleteMapping("/{stopId}")
    public Mono<ResponseEntity<Void>> deactivateStop(
            @PathVariable UUID stopId
    ) {
        return adminStopService.deactivateStop(stopId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
