package com.reset.bus_tracker_interface.repository;

import com.reset.bus_tracker_interface.domain.BusStop;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BusStopRepository extends ReactiveCrudRepository<BusStop, UUID> {

    Mono<BusStop> findByStopCode(String stopCode);
}
