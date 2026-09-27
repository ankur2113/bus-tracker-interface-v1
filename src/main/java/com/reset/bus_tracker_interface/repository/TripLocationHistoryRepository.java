package com.reset.bus_tracker_interface.repository;

import com.reset.bus_tracker_interface.domain.TripLocationHistory;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface TripLocationHistoryRepository extends ReactiveCrudRepository<TripLocationHistory, UUID> {
    Flux<TripLocationHistory> findTop100ByTripIdOrderByLocationRecordedAtDesc(
            UUID tripId
    );
}
