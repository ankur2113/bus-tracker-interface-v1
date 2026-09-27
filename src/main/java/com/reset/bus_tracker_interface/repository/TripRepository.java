package com.reset.bus_tracker_interface.repository;

import com.reset.bus_tracker_interface.domain.Trip;
import com.reset.bus_tracker_interface.domain.TripStatus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.UUID;

public interface TripRepository extends ReactiveCrudRepository<Trip, UUID> {

    Flux<Trip> findByRouteIdAndStatusIn(
            UUID routeId,
            Collection<TripStatus> statuses
    );

    Flux<Trip> findByRouteIdInAndStatusIn(
            Collection<UUID> routeIds,
            Collection<TripStatus> statuses
    );

    Mono<Trip> findFirstByCoordinatorIdAndStatusIn(
            UUID coordinatorId,
            Collection<TripStatus> statuses
    );

    Mono<Trip> findFirstByBusIdAndStatusIn(
            UUID busId,
            Collection<TripStatus> statuses
    );

    Flux<Trip> findByCoordinatorIdOrderByScheduledStartAtDesc(
            UUID coordinatorId
    );

    Flux<Trip> findByStatusIn(Collection<TripStatus> statuses);
}