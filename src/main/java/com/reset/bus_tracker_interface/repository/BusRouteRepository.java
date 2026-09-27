package com.reset.bus_tracker_interface.repository;

import com.reset.bus_tracker_interface.domain.BusRoute;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BusRouteRepository extends ReactiveCrudRepository<BusRoute, UUID> {
    Mono<BusRoute> findByRouteCode(String routeCode);
}
