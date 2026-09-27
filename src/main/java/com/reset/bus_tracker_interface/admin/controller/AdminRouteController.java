package com.reset.bus_tracker_interface.admin.controller;

import java.net.URI;
import java.util.UUID;

import com.reset.bus_tracker_interface.admin.dto.AddRouteStopRequest;
import com.reset.bus_tracker_interface.admin.dto.CreateRouteRequest;
import com.reset.bus_tracker_interface.admin.dto.RouteDetailsResponse;
import com.reset.bus_tracker_interface.admin.dto.RouteResponse;
import com.reset.bus_tracker_interface.admin.dto.RouteStopResponse;
import com.reset.bus_tracker_interface.admin.dto.UpdateRouteRequest;

import com.reset.bus_tracker_interface.admin.service.AdminRouteService;
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
@RequestMapping("/api/v1/admin/routes")
public class AdminRouteController {

    private final AdminRouteService adminRouteService;

    public AdminRouteController(AdminRouteService adminRouteService) {
        this.adminRouteService = adminRouteService;
    }

    @GetMapping
    public Flux<RouteResponse> listRoutes() {
        return adminRouteService.listRoutes();
    }

    @GetMapping("/{routeId}")
    public Mono<RouteDetailsResponse> getRouteDetails(
            @PathVariable UUID routeId
    ) {
        return adminRouteService.getRouteDetails(routeId);
    }

    @PostMapping
    public Mono<ResponseEntity<RouteResponse>> createRoute(
            @Valid @RequestBody CreateRouteRequest request
    ) {
        return adminRouteService.createRoute(request)
                .map(response -> ResponseEntity
                        .created(URI.create(
                                "/api/v1/admin/routes/" + response.id()
                        ))
                        .body(response));
    }

    @PutMapping("/{routeId}")
    public Mono<RouteResponse> updateRoute(
            @PathVariable UUID routeId,
            @Valid @RequestBody UpdateRouteRequest request
    ) {
        return adminRouteService.updateRoute(routeId, request);
    }

    @DeleteMapping("/{routeId}")
    public Mono<ResponseEntity<Void>> deactivateRoute(
            @PathVariable UUID routeId
    ) {
        return adminRouteService.deactivateRoute(routeId)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PostMapping("/{routeId}/stops")
    public Mono<ResponseEntity<RouteStopResponse>> addStopToRoute(
            @PathVariable UUID routeId,
            @Valid @RequestBody AddRouteStopRequest request
    ) {
        return adminRouteService.addStopToRoute(routeId, request)
                .map(response -> ResponseEntity
                        .created(URI.create(
                                "/api/v1/admin/routes/"
                                        + routeId
                                        + "/stops/"
                                        + response.routeStopId()
                        ))
                        .body(response));
    }

    @DeleteMapping("/{routeId}/stops/{routeStopId}")
    public Mono<ResponseEntity<Void>> removeRouteStop(
            @PathVariable UUID routeId,
            @PathVariable UUID routeStopId
    ) {
        return adminRouteService.removeRouteStop(routeStopId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
