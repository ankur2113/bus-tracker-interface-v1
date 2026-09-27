package com.reset.bus_tracker_interface.repository;

import com.reset.bus_tracker_interface.domain.TripCurrentLocation;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TripCurrentLocationRepository extends ReactiveCrudRepository<TripCurrentLocation, UUID> {
    Mono<TripCurrentLocation> findByTripId(UUID tripId);
}
