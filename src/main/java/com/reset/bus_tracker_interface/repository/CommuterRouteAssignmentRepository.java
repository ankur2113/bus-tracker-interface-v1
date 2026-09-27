package com.reset.bus_tracker_interface.repository;

import java.util.UUID;

import com.reset.bus_tracker_interface.domain.CommuterRouteAssignment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CommuterRouteAssignmentRepository
        extends ReactiveCrudRepository<CommuterRouteAssignment, UUID> {

    Flux<CommuterRouteAssignment> findByCommuterIdAndActiveTrue(
            UUID commuterId
    );

    Flux<CommuterRouteAssignment> findByRouteIdAndActiveTrue(
            UUID routeId
    );

    Mono<CommuterRouteAssignment> findByCommuterIdAndRouteId(
            UUID commuterId,
            UUID routeId
    );
}