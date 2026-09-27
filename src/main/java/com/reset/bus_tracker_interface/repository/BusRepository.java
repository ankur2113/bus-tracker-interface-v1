package com.reset.bus_tracker_interface.repository;

import com.reset.bus_tracker_interface.domain.Bus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BusRepository extends ReactiveCrudRepository<Bus, UUID> {

    Mono<Bus> findByRegistrationNumber(String registrationNumber);
 }
