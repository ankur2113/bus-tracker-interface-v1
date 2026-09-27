package com.reset.bus_tracker_interface.emergency;

import com.reset.bus_tracker_interface.api.error.NotFoundException;
import com.reset.bus_tracker_interface.domain.EmergencyReport;
import com.reset.bus_tracker_interface.domain.EmergencyStatus;
import com.reset.bus_tracker_interface.emergency.dto.CreateEmergencyReportRequest;
import com.reset.bus_tracker_interface.emergency.dto.EmergencyReportResponse;
import com.reset.bus_tracker_interface.repository.EmergencyReportRepository;
import com.reset.bus_tracker_interface.repository.TripRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
public class EmergencyReportService {
    private final EmergencyReportRepository emergencyReportRepository;
    private final TripRepository tripRepository;

    public EmergencyReportService(
            EmergencyReportRepository emergencyReportRepository,
            TripRepository tripRepository
    ) {
        this.emergencyReportRepository = emergencyReportRepository;
        this.tripRepository = tripRepository;
    }

    public Mono<EmergencyReportResponse> reportEmergency(
            UUID coordinatorId,
            UUID tripId,
            CreateEmergencyReportRequest request
    ) {
        return tripRepository.findById(tripId)
                .filter(trip -> trip.coordinatorId().equals(coordinatorId))
                .switchIfEmpty(Mono.error(new NotFoundException("Trip not found for this coordinator.")))
                .flatMap(trip -> emergencyReportRepository.save(new EmergencyReport(
                        null,
                        trip.id(),
                        coordinatorId,
                        request.emergencyType().trim(),
                        request.message().trim(),
                        EmergencyStatus.OPEN,
                        Instant.now(),
                        null,
                        null,
                        null,
                        null,
                        null
                )))
                .map(EmergencyReportMapper::toResponse);
    }

    public Flux<EmergencyReportResponse> listOpenEmergencies() {
        return emergencyReportRepository
                .findByStatusOrderByReportedAtDesc(EmergencyStatus.OPEN)
                .map(EmergencyReportMapper::toResponse);
    }

    public Flux<EmergencyReportResponse> listTripEmergencies(
            UUID coordinatorId,
            UUID tripId
    ) {
        return tripRepository.findById(tripId)
                .filter(trip -> trip.coordinatorId().equals(coordinatorId))
                .switchIfEmpty(Mono.error(new NotFoundException("Trip not found for this coordinator.")))
                .thenMany(emergencyReportRepository.findByTripIdOrderByReportedAtDesc(tripId))
                .map(EmergencyReportMapper::toResponse);
    }

    public Mono<EmergencyReportResponse> resolveEmergency(
            UUID adminId,
            UUID emergencyId
    ) {
        return emergencyReportRepository.findById(emergencyId)
                .switchIfEmpty(Mono.error(new NotFoundException("Emergency report not found.")))
                .flatMap(existing -> {
                    if (existing.status() == EmergencyStatus.RESOLVED) {
                        return Mono.just(existing);
                    }

                    EmergencyReport resolved = new EmergencyReport(
                            existing.id(),
                            existing.tripId(),
                            existing.reportedByUserId(),
                            existing.emergencyType(),
                            existing.message(),
                            EmergencyStatus.RESOLVED,
                            existing.reportedAt(),
                            Instant.now(),
                            adminId,
                            existing.createdAt(),
                            existing.updatedAt(),
                            existing.version()
                    );

                    return emergencyReportRepository.save(resolved);
                })
                .map(EmergencyReportMapper::toResponse);
    }
}
