package com.reset.bus_tracker_interface.repository;

import com.reset.bus_tracker_interface.domain.RouteStop;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RouteStopRepository extends ReactiveCrudRepository<RouteStop, UUID> {
    Flux<RouteStop> findByRouteIdOrderByStopSequence(UUID routeId);
    Mono<RouteStop> findByRouteIdAndStopId(UUID routeId, UUID stopId);
}
