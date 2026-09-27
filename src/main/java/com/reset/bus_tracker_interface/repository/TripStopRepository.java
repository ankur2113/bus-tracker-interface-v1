package com.reset.bus_tracker_interface.repository;

import com.reset.bus_tracker_interface.domain.TripStop;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TripStopRepository extends ReactiveCrudRepository<TripStop, UUID> {
    Flux<TripStop> findByTripIdOrderByStopSequence(UUID tripId);

    Mono<TripStop> findByIdAndTripId(UUID id, UUID tripId);
}
