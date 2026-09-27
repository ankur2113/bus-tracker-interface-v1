package com.reset.bus_tracker_interface.admin.service;

import java.util.Locale;
import java.util.UUID;

import com.reset.bus_tracker_interface.admin.AdminMapper;
import com.reset.bus_tracker_interface.admin.dto.BusResponse;
import com.reset.bus_tracker_interface.admin.dto.CreateBusRequest;
import com.reset.bus_tracker_interface.admin.dto.UpdateBusRequest;
import com.reset.bus_tracker_interface.api.error.ConflictException;
import com.reset.bus_tracker_interface.api.error.NotFoundException;
import com.reset.bus_tracker_interface.domain.Bus;
import com.reset.bus_tracker_interface.repository.BusRepository;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AdminBusService {

    private final BusRepository busRepository;

    public AdminBusService(BusRepository busRepository) {
        this.busRepository = busRepository;
    }

    public Flux<BusResponse> listBuses() {
        return busRepository.findAll()
                .map(AdminMapper::toBusResponse);
    }

    public Mono<BusResponse> getBus(UUID busId) {
        return busRepository.findById(busId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Bus not found.")
                ))
                .map(AdminMapper::toBusResponse);
    }

    public Mono<BusResponse> createBus(CreateBusRequest request) {
        String registrationNumber = request.registrationNumber()
                .strip()
                .toUpperCase(Locale.ROOT);

        return busRepository.findByRegistrationNumber(registrationNumber)
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ConflictException(
                                "Bus registration number already exists."
                        ));
                    }

                    Bus bus = new Bus(
                            null,
                            registrationNumber,
                            request.displayName(),
                            request.capacity(),
                            true,
                            null,
                            null,
                            null
                    );

                    return busRepository.save(bus)
                            .map(AdminMapper::toBusResponse);
                });
    }

    public Mono<BusResponse> updateBus(
            UUID busId,
            UpdateBusRequest request
    ) {
        return busRepository.findById(busId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Bus not found.")
                ))
                .flatMap(existing -> {
                    Bus updated = new Bus(
                            existing.id(),
                            existing.registrationNumber(),
                            request.displayName() == null
                                    ? existing.displayName()
                                    : request.displayName(),
                            request.capacity() == null
                                    ? existing.capacity()
                                    : request.capacity(),
                            request.active() == null
                                    ? existing.active()
                                    : request.active(),
                            existing.createdAt(),
                            existing.updatedAt(),
                            existing.version()
                    );

                    return busRepository.save(updated);
                })
                .map(AdminMapper::toBusResponse);
    }

    public Mono<Void> deactivateBus(UUID busId) {
        return busRepository.findById(busId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Bus not found.")
                ))
                .flatMap(existing -> {
                    Bus updated = new Bus(
                            existing.id(),
                            existing.registrationNumber(),
                            existing.displayName(),
                            existing.capacity(),
                            false,
                            existing.createdAt(),
                            existing.updatedAt(),
                            existing.version()
                    );

                    return busRepository.save(updated);
                })
                .then();
    }
}
