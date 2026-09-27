package com.reset.bus_tracker_interface.repository;

import com.reset.bus_tracker_interface.domain.CheckInStatus;
import com.reset.bus_tracker_interface.domain.CommuterCheckIn;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CommuterCheckInRepository
        extends ReactiveCrudRepository<CommuterCheckIn, UUID> {

    Mono<CommuterCheckIn> findByTripIdAndCommuterId(
            UUID tripId,
            UUID commuterId
    );

    Flux<CommuterCheckIn> findByTripIdOrderByCheckedInAtAsc(UUID tripId);

    Flux<CommuterCheckIn> findByTripIdAndTripStopIdOrderByCheckedInAtAsc(
            UUID tripId,
            UUID tripStopId
    );

    Flux<CommuterCheckIn> findByTripStopIdAndStatus(
            UUID tripStopId,
            CheckInStatus status
    );

    Flux<CommuterCheckIn> findByCommuterIdOrderByCheckedInAtDesc(
            UUID commuterId
    );
}
