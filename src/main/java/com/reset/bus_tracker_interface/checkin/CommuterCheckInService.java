package com.reset.bus_tracker_interface.checkin;

import java.time.Instant;
import java.util.UUID;

import com.reset.bus_tracker_interface.api.error.BadRequestException;
import com.reset.bus_tracker_interface.api.error.ConflictException;
import com.reset.bus_tracker_interface.api.error.NotFoundException;
import com.reset.bus_tracker_interface.checkin.dto.CheckInRequest;
import com.reset.bus_tracker_interface.checkin.dto.CheckInResponse;
import com.reset.bus_tracker_interface.domain.CheckInStatus;
import com.reset.bus_tracker_interface.domain.CommuterCheckIn;
import com.reset.bus_tracker_interface.domain.Trip;
import com.reset.bus_tracker_interface.domain.TripStop;
import com.reset.bus_tracker_interface.domain.TripStopStatus;
import com.reset.bus_tracker_interface.repository.CommuterCheckInRepository;
import com.reset.bus_tracker_interface.repository.TripStopRepository;
import com.reset.bus_tracker_interface.trip.CommuterTripService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CommuterCheckInService {

    private final CommuterTripService commuterTripService;
    private final TripStopRepository tripStopRepository;
    private final CommuterCheckInRepository checkInRepository;
    private final CheckInAssembler checkInAssembler;

    public CommuterCheckInService(
            CommuterTripService commuterTripService,
            TripStopRepository tripStopRepository,
            CommuterCheckInRepository checkInRepository,
            CheckInAssembler checkInAssembler
    ) {
        this.commuterTripService = commuterTripService;
        this.tripStopRepository = tripStopRepository;
        this.checkInRepository = checkInRepository;
        this.checkInAssembler = checkInAssembler;
    }

    public Flux<CheckInResponse> listMyCheckIns(UUID commuterId) {
        return checkInRepository
                .findByCommuterIdOrderByCheckedInAtDesc(commuterId)
                .flatMap(checkInAssembler::toResponse);
    }

    @Transactional
    public Mono<CheckInResponse> checkIn(
            UUID commuterId,
            UUID tripId,
            CheckInRequest request
    ) {
        Mono<Trip> tripMono = commuterTripService.getVisibleTrip(
                commuterId,
                tripId
        );

        Mono<TripStop> tripStopMono = tripStopRepository
                .findByIdAndTripId(request.tripStopId(), tripId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Trip stop not found.")
                ));

        return Mono.zip(tripMono, tripStopMono)
                .flatMap(tuple -> {
                    TripStop tripStop = tuple.getT2();

                    if (
                            tripStop.status() == TripStopStatus.DEPARTED
                                    || tripStop.status() == TripStopStatus.SKIPPED
                    ) {
                        return Mono.error(new BadRequestException(
                                "Cannot check in for a departed or skipped stop."
                        ));
                    }

                    return createOrReactivateCheckIn(
                            commuterId,
                            tripId,
                            tripStop.id()
                    );
                })
                .flatMap(checkInAssembler::toResponse);
    }

    @Transactional
    public Mono<CheckInResponse> cancelCheckIn(
            UUID commuterId,
            UUID tripId
    ) {
        return checkInRepository.findByTripIdAndCommuterId(
                        tripId,
                        commuterId
                )
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Check-in not found.")
                ))
                .flatMap(existing -> {
                    if (existing.status() == CheckInStatus.BOARDED) {
                        return Mono.error(new BadRequestException(
                                "Boarded check-in cannot be cancelled."
                        ));
                    }

                    if (existing.status() == CheckInStatus.CANCELLED) {
                        return Mono.error(new BadRequestException(
                                "Check-in is already cancelled."
                        ));
                    }

                    CommuterCheckIn cancelled = new CommuterCheckIn(
                            existing.id(),
                            existing.tripId(),
                            existing.tripStopId(),
                            existing.commuterId(),
                            CheckInStatus.CANCELLED,
                            existing.checkedInAt(),
                            existing.boardedAt(),
                            Instant.now(),
                            existing.createdAt(),
                            existing.updatedAt(),
                            existing.version()
                    );

                    return checkInRepository.save(cancelled);
                })
                .flatMap(checkInAssembler::toResponse);
    }

    private Mono<CommuterCheckIn> createOrReactivateCheckIn(
            UUID commuterId,
            UUID tripId,
            UUID tripStopId
    ) {
        return checkInRepository.findByTripIdAndCommuterId(tripId, commuterId)
                .flatMap(existing -> {
                    if (
                            existing.status() == CheckInStatus.CHECKED_IN
                                    || existing.status() == CheckInStatus.BOARDED
                    ) {
                        return Mono.error(new ConflictException(
                                "Commuter is already checked in for this trip."
                        ));
                    }

                    CommuterCheckIn reactivated = new CommuterCheckIn(
                            existing.id(),
                            tripId,
                            tripStopId,
                            commuterId,
                            CheckInStatus.CHECKED_IN,
                            Instant.now(),
                            null,
                            null,
                            existing.createdAt(),
                            existing.updatedAt(),
                            existing.version()
                    );

                    return checkInRepository.save(reactivated);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    CommuterCheckIn checkIn = new CommuterCheckIn(
                            null,
                            tripId,
                            tripStopId,
                            commuterId,
                            CheckInStatus.CHECKED_IN,
                            Instant.now(),
                            null,
                            null,
                            null,
                            null,
                            null
                    );

                    return checkInRepository.save(checkIn);
                }));
    }
}