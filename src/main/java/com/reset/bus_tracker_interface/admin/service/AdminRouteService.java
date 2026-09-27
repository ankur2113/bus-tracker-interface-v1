package com.reset.bus_tracker_interface.admin.service;

import com.reset.bus_tracker_interface.admin.AdminMapper;
import com.reset.bus_tracker_interface.admin.dto.*;
import com.reset.bus_tracker_interface.api.error.ConflictException;
import com.reset.bus_tracker_interface.api.error.NotFoundException;
import com.reset.bus_tracker_interface.domain.BusRoute;
import com.reset.bus_tracker_interface.domain.BusStop;
import com.reset.bus_tracker_interface.domain.RouteStop;
import com.reset.bus_tracker_interface.repository.BusRouteRepository;
import com.reset.bus_tracker_interface.repository.BusStopRepository;
import com.reset.bus_tracker_interface.repository.RouteStopRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.Locale;
import java.util.UUID;

@Service
public class AdminRouteService {

    private final BusRouteRepository busRouteRepository;
    private final BusStopRepository busStopRepository;
    private final RouteStopRepository routeStopRepository;

    public AdminRouteService(
            BusRouteRepository busRouteRepository,
            BusStopRepository busStopRepository,
            RouteStopRepository routeStopRepository
    ) {
        this.busRouteRepository = busRouteRepository;
        this.busStopRepository = busStopRepository;
        this.routeStopRepository = routeStopRepository;
    }

    public Flux<RouteResponse> listRoutes() {
        return busRouteRepository.findAll()
                .map(AdminMapper::toRouteResponse);
    }

    public Mono<RouteDetailsResponse> getRouteDetails(UUID routeId) {
        Mono<BusRoute> routeMono = busRouteRepository.findById(routeId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Route not found.")
                ));

        Mono<java.util.List<RouteStopResponse>> stopsMono =
                routeStopRepository.findByRouteIdOrderByStopSequence(routeId)
                        .flatMap(this::toRouteStopResponse)
                        .sort(Comparator.comparing(
                                RouteStopResponse::stopSequence
                        ))
                        .collectList();

        return Mono.zip(routeMono, stopsMono)
                .map(tuple -> new RouteDetailsResponse(
                        AdminMapper.toRouteResponse(tuple.getT1()),
                        tuple.getT2()
                ));
    }

    public Mono<RouteResponse> createRoute(CreateRouteRequest request) {
        String routeCode = request.routeCode()
                .strip()
                .toUpperCase(Locale.ROOT);

        return busRouteRepository.findByRouteCode(routeCode)
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ConflictException(
                                "Route code already exists."
                        ));
                    }

                    BusRoute route = new BusRoute(
                            null,
                            routeCode,
                            request.routeName(),
                            request.description(),
                            true,
                            null,
                            null,
                            null
                    );

                    return busRouteRepository.save(route)
                            .map(AdminMapper::toRouteResponse);
                });
    }

    public Mono<RouteResponse> updateRoute(
            UUID routeId,
            UpdateRouteRequest request
    ) {
        return busRouteRepository.findById(routeId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Route not found.")
                ))
                .flatMap(existing -> {
                    BusRoute updated = new BusRoute(
                            existing.id(),
                            existing.routeCode(),
                            request.routeName(),
                            request.description(),
                            request.active() == null
                                    ? existing.active()
                                    : request.active(),
                            existing.createdAt(),
                            existing.updatedAt(),
                            existing.version()
                    );

                    return busRouteRepository.save(updated);
                })
                .map(AdminMapper::toRouteResponse);
    }

    public Mono<RouteStopResponse> addStopToRoute(
            UUID routeId,
            AddRouteStopRequest request
    ) {
        Mono<BusRoute> routeMono = busRouteRepository.findById(routeId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Route not found.")
                ));

        Mono<BusStop> stopMono = busStopRepository.findById(request.stopId())
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Stop not found.")
                ));

        return Mono.zip(routeMono, stopMono)
                .flatMap(tuple -> {
                    RouteStop routeStop = new RouteStop(
                            null,
                            routeId,
                            request.stopId(),
                            request.stopSequence(),
                            request.plannedArrivalOffsetMinutes(),
                            request.pickupEnabled() == null
                                    ? true
                                    : request.pickupEnabled(),
                            null,
                            null,
                            null
                    );

                    return routeStopRepository.save(routeStop);
                })
                .flatMap(this::toRouteStopResponse);
    }

    public Mono<Void> removeRouteStop(UUID routeStopId) {
        return routeStopRepository.findById(routeStopId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Route stop not found.")
                ))
                .flatMap(routeStopRepository::delete);
    }

    public Mono<Void> deactivateRoute(UUID routeId) {
        return busRouteRepository.findById(routeId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Route not found.")
                ))
                .flatMap(existing -> {
                    BusRoute updated = new BusRoute(
                            existing.id(),
                            existing.routeCode(),
                            existing.routeName(),
                            existing.description(),
                            false,
                            existing.createdAt(),
                            existing.updatedAt(),
                            existing.version()
                    );

                    return busRouteRepository.save(updated);
                })
                .then();
    }

    private Mono<RouteStopResponse> toRouteStopResponse(RouteStop routeStop) {
        return busStopRepository.findById(routeStop.stopId())
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Stop not found for route stop.")
                ))
                .map(stop -> new RouteStopResponse(
                        routeStop.id(),
                        stop.id(),
                        stop.stopCode(),
                        stop.stopName(),
                        stop.landmark(),
                        stop.latitude(),
                        stop.longitude(),
                        routeStop.stopSequence(),
                        routeStop.plannedArrivalOffsetMinutes(),
                        routeStop.pickupEnabled()
                ));
    }
}
