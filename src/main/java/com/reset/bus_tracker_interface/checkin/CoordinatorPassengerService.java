package com.reset.bus_tracker_interface.checkin;

import java.time.Instant;
import java.util.Comparator;
import java.util.UUID;

import com.reset.bus_tracker_interface.api.error.BadRequestException;
import com.reset.bus_tracker_interface.api.error.NotFoundException;
import com.reset.bus_tracker_interface.checkin.dto.CheckInResponse;
import com.reset.bus_tracker_interface.checkin.dto.StopPassengerManifestResponse;
import com.reset.bus_tracker_interface.domain.BusStop;
import com.reset.bus_tracker_interface.domain.CheckInStatus;
import com.reset.bus_tracker_interface.domain.CommuterCheckIn;
import com.reset.bus_tracker_interface.domain.Trip;
import com.reset.bus_tracker_interface.domain.TripStop;
import com.reset.bus_tracker_interface.repository.BusStopRepository;
import com.reset.bus_tracker_interface.repository.CommuterCheckInRepository;
import com.reset.bus_tracker_interface.repository.TripRepository;
import com.reset.bus_tracker_interface.repository.TripStopRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CoordinatorPassengerService {

    private final TripRepository tripRepository;
    private final TripStopRepository tripStopRepository;
    private final BusStopRepository busStopRepository;
    private final CommuterCheckInRepository checkInRepository;
    private final CheckInAssembler checkInAssembler;

    public CoordinatorPassengerService(
            TripRepository tripRepository,
            TripStopRepository tripStopRepository,
            BusStopRepository busStopRepository,
            CommuterCheckInRepository checkInRepository,
            CheckInAssembler checkInAssembler
    ) {
        this.tripRepository = tripRepository;
        this.tripStopRepository = tripStopRepository;
        this.busStopRepository = busStopRepository;
        this.checkInRepository = checkInRepository;
        this.checkInAssembler = checkInAssembler;
    }

    public Flux<StopPassengerManifestResponse> getPassengerManifest(
            UUID coordinatorId,
            UUID tripId
    ) {
        return getOwnedTrip(coordinatorId, tripId)
                .thenMany(tripStopRepository
                        .findByTripIdOrderByStopSequence(tripId)
                )
                .flatMap(this::toManifestStop)
                .sort(Comparator.comparing(
                        StopPassengerManifestResponse::stopSequence
                ));
    }

    public Flux<CheckInResponse> getCheckInsForStop(
            UUID coordinatorId,
            UUID tripId,
            UUID tripStopId
    ) {
        return getOwnedTrip(coordinatorId, tripId)
                .thenMany(checkInRepository
                        .findByTripIdAndTripStopIdOrderByCheckedInAtAsc(
                                tripId,
                                tripStopId
                        )
                )
                .flatMap(checkInAssembler::toResponse);
    }

    @Transactional
    public Mono<CheckInResponse> markBoarded(
            UUID coordinatorId,
            UUID tripId,
            UUID checkInId
    ) {
        return getOwnedTrip(coordinatorId, tripId)
                .then(getCheckInForTrip(tripId, checkInId))
                .flatMap(existing -> {
                    if (existing.status() != CheckInStatus.CHECKED_IN) {
                        return Mono.error(new BadRequestException(
                                "Only CHECKED_IN commuters can be marked BOARDED."
                        ));
                    }

                    CommuterCheckIn boarded = new CommuterCheckIn(
                            existing.id(),
                            existing.tripId(),
                            existing.tripStopId(),
                            existing.commuterId(),
                            CheckInStatus.BOARDED,
                            existing.checkedInAt(),
                            Instant.now(),
                            existing.cancelledAt(),
                            existing.createdAt(),
                            existing.updatedAt(),
                            existing.version()
                    );

                    return checkInRepository.save(boarded);
                })
                .flatMap(checkInAssembler::toResponse);
    }

    @Transactional
    public Mono<CheckInResponse> markNoShow(
            UUID coordinatorId,
            UUID tripId,
            UUID checkInId
    ) {
        return getOwnedTrip(coordinatorId, tripId)
                .then(getCheckInForTrip(tripId, checkInId))
                .flatMap(existing -> {
                    if (existing.status() != CheckInStatus.CHECKED_IN) {
                        return Mono.error(new BadRequestException(
                                "Only CHECKED_IN commuters can be marked NO_SHOW."
                        ));
                    }

                    CommuterCheckIn noShow = new CommuterCheckIn(
                            existing.id(),
                            existing.tripId(),
                            existing.tripStopId(),
                            existing.commuterId(),
                            CheckInStatus.NO_SHOW,
                            existing.checkedInAt(),
                            existing.boardedAt(),
                            existing.cancelledAt(),
                            existing.createdAt(),
                            existing.updatedAt(),
                            existing.version()
                    );

                    return checkInRepository.save(noShow);
                })
                .flatMap(checkInAssembler::toResponse);
    }

    private Mono<Trip> getOwnedTrip(UUID coordinatorId, UUID tripId) {
        return tripRepository.findById(tripId)
                .filter(trip -> trip.coordinatorId().equals(coordinatorId))
                .switchIfEmpty(Mono.error(
                        new NotFoundException(
                                "Trip not found for this coordinator."
                        )
                ));
    }

    private Mono<CommuterCheckIn> getCheckInForTrip(
            UUID tripId,
            UUID checkInId
    ) {
        return checkInRepository.findById(checkInId)
                .filter(checkIn -> checkIn.tripId().equals(tripId))
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Check-in not found.")
                ));
    }

    private Mono<StopPassengerManifestResponse> toManifestStop(
            TripStop tripStop
    ) {
        Mono<BusStop> stopMono = busStopRepository.findById(tripStop.stopId())
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Stop not found.")
                ));

        Mono<java.util.List<CheckInResponse>> passengersMono =
                checkInRepository
                        .findByTripIdAndTripStopIdOrderByCheckedInAtAsc(
                                tripStop.tripId(),
                                tripStop.id()
                        )
                        .flatMap(checkInAssembler::toResponse)
                        .collectList();

        return Mono.zip(stopMono, passengersMono)
                .map(tuple -> {
                    BusStop stop = tuple.getT1();

                    return new StopPassengerManifestResponse(
                            tripStop.id(),
                            stop.id(),
                            stop.stopCode(),
                            stop.stopName(),
                            tripStop.stopSequence(),
                            tripStop.status(),
                            tuple.getT2()
                    );
                });
    }
}