package com.reset.bus_tracker_interface.repository;

import com.reset.bus_tracker_interface.domain.EmergencyReport;
import com.reset.bus_tracker_interface.domain.EmergencyStatus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface EmergencyReportRepository extends ReactiveCrudRepository<EmergencyReport, UUID> {
    Flux<EmergencyReport> findByStatusOrderByReportedAtDesc(EmergencyStatus status);
    Flux<EmergencyReport> findByTripIdOrderByReportedAtDesc(UUID tripId);
}
