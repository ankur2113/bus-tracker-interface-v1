package com.reset.bus_tracker_interface.admin.service;

import java.util.Locale;
import java.util.UUID;

import com.reset.bus_tracker_interface.admin.AdminMapper;
import com.reset.bus_tracker_interface.admin.dto.CreateStopRequest;
import com.reset.bus_tracker_interface.admin.dto.StopResponse;
import com.reset.bus_tracker_interface.admin.dto.UpdateStopRequest;
import com.reset.bus_tracker_interface.api.error.ConflictException;
import com.reset.bus_tracker_interface.api.error.NotFoundException;
import com.reset.bus_tracker_interface.domain.BusStop;
import com.reset.bus_tracker_interface.repository.BusStopRepository;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AdminStopService {

    private final BusStopRepository busStopRepository;

    public AdminStopService(BusStopRepository busStopRepository) {
        this.busStopRepository = busStopRepository;
    }

    public Flux<StopResponse> listStops() {
        return busStopRepository.findAll()
                .map(AdminMapper::toStopResponse);
    }

    public Mono<StopResponse> getStop(UUID stopId) {
        return busStopRepository.findById(stopId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Stop not found.")
                ))
                .map(AdminMapper::toStopResponse);
    }

    public Mono<StopResponse> createStop(CreateStopRequest request) {
        String stopCode = normalizeCode(request.stopCode());

        Mono<Boolean> duplicateCheck = stopCode == null
                ? Mono.just(false)
                : busStopRepository.findByStopCode(stopCode).hasElement();

        return duplicateCheck.flatMap(exists -> {
            if (exists) {
                return Mono.error(new ConflictException(
                        "Stop code already exists."
                ));
            }

            BusStop stop = new BusStop(
                    null,
                    stopCode,
                    request.stopName(),
                    request.landmark(),
                    request.latitude(),
                    request.longitude(),
                    true,
                    null,
                    null,
                    null
            );

            return busStopRepository.save(stop)
                    .map(AdminMapper::toStopResponse);
        });
    }

    public Mono<StopResponse> updateStop(
            UUID stopId,
            UpdateStopRequest request
    ) {
        return busStopRepository.findById(stopId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Stop not found.")
                ))
                .flatMap(existing -> {
                    BusStop updated = new BusStop(
                            existing.id(),
                            existing.stopCode(),
                            request.stopName(),
                            request.landmark(),
                            request.latitude(),
                            request.longitude(),
                            request.active() == null
                                    ? existing.active()
                                    : request.active(),
                            existing.createdAt(),
                            existing.updatedAt(),
                            existing.version()
                    );

                    return busStopRepository.save(updated);
                })
                .map(AdminMapper::toStopResponse);
    }

    public Mono<Void> deactivateStop(UUID stopId) {
        return busStopRepository.findById(stopId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Stop not found.")
                ))
                .flatMap(existing -> {
                    BusStop updated = new BusStop(
                            existing.id(),
                            existing.stopCode(),
                            existing.stopName(),
                            existing.landmark(),
                            existing.latitude(),
                            existing.longitude(),
                            false,
                            existing.createdAt(),
                            existing.updatedAt(),
                            existing.version()
                    );

                    return busStopRepository.save(updated);
                })
                .then();
    }

    private String normalizeCode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.strip().toUpperCase(Locale.ROOT);
    }
}