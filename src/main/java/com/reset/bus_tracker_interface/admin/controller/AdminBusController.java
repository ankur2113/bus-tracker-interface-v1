package com.reset.bus_tracker_interface.admin.controller;

import java.net.URI;
import java.util.UUID;

import com.reset.bus_tracker_interface.admin.dto.BusResponse;
import com.reset.bus_tracker_interface.admin.dto.CreateBusRequest;
import com.reset.bus_tracker_interface.admin.dto.UpdateBusRequest;

import com.reset.bus_tracker_interface.admin.service.AdminBusService;
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
@RequestMapping("/api/v1/admin/buses")
public class AdminBusController {

    private final AdminBusService adminBusService;

    public AdminBusController(AdminBusService adminBusService) {
        this.adminBusService = adminBusService;
    }

    @GetMapping
    public Flux<BusResponse> listBuses() {
        return adminBusService.listBuses();
    }

    @GetMapping("/{busId}")
    public Mono<BusResponse> getBus(@PathVariable UUID busId) {
        return adminBusService.getBus(busId);
    }

    @PostMapping
    public Mono<ResponseEntity<BusResponse>> createBus(
            @Valid @RequestBody CreateBusRequest request
    ) {
        return adminBusService.createBus(request)
                .map(response -> ResponseEntity
                        .created(URI.create(
                                "/api/v1/admin/buses/" + response.id()
                        ))
                        .body(response));
    }

    @PutMapping("/{busId}")
    public Mono<BusResponse> updateBus(
            @PathVariable UUID busId,
            @Valid @RequestBody UpdateBusRequest request
    ) {
        return adminBusService.updateBus(busId, request);
    }

    @DeleteMapping("/{busId}")
    public Mono<ResponseEntity<Void>> deactivateBus(
            @PathVariable UUID busId
    ) {
        return adminBusService.deactivateBus(busId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
