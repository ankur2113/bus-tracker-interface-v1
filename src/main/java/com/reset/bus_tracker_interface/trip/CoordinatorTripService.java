package com.reset.bus_tracker_interface.trip;

import java.time.Instant;
import java.util.UUID;

import com.reset.bus_tracker_interface.api.error.BadRequestException;
import com.reset.bus_tracker_interface.api.error.NotFoundException;
import com.reset.bus_tracker_interface.domain.Trip;
import com.reset.bus_tracker_interface.domain.TripStatus;
import com.reset.bus_tracker_interface.domain.TripStop;
import com.reset.bus_tracker_interface.domain.TripStopStatus;
import com.reset.bus_tracker_interface.repository.TripRepository;
import com.reset.bus_tracker_interface.repository.TripStopRepository;
import com.reset.bus_tracker_interface.trip.dto.TripDetailsResponse;
import com.reset.bus_tracker_interface.trip.dto.TripResponse;
import com.reset.bus_tracker_interface.trip.dto.TripStopResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CoordinatorTripService {

    private final TripRepository tripRepository;
    private final TripStopRepository tripStopRepository;
    private final TripDetailsAssembler tripDetailsAssembler;

    public CoordinatorTripService(
            TripRepository tripRepository,
            TripStopRepository tripStopRepository,
            TripDetailsAssembler tripDetailsAssembler
    ) {
        this.tripRepository = tripRepository;
        this.tripStopRepository = tripStopRepository;
        this.tripDetailsAssembler = tripDetailsAssembler;
    }

    public Flux<TripResponse> listAssignedTrips(UUID coordinatorId) {
        return tripRepository
                .findByCoordinatorIdOrderByScheduledStartAtDesc(coordinatorId)
                .flatMap(tripDetailsAssembler::toResponse);
    }

    public Mono<TripDetailsResponse> getAssignedTripDetails(
            UUID coordinatorId,
            UUID tripId
    ) {
        return getOwnedTrip(coordinatorId, tripId)
                .flatMap(tripDetailsAssembler::toDetails);
    }

    @Transactional
    public Mono<TripDetailsResponse> markBoarding(
            UUID coordinatorId,
            UUID tripId
    ) {
        return getOwnedTrip(coordinatorId, tripId)
                .flatMap(existing -> {
                    if (existing.status() != TripStatus.PLANNED) {
                        return Mono.error(new BadRequestException(
                                "Only PLANNED trips can be moved to BOARDING."
                        ));
                    }

                    Trip updated = copyWithStatus(
                            existing,
                            TripStatus.BOARDING,
                            existing.actualStartAt(),
                            existing.actualEndAt()
                    );

                    return tripRepository.save(updated);
                })
                .flatMap(tripDetailsAssembler::toDetails);
    }

    @Transactional
    public Mono<TripDetailsResponse> startTrip(
            UUID coordinatorId,
            UUID tripId
    ) {
        return getOwnedTrip(coordinatorId, tripId)
                .flatMap(existing -> {
                    if (
                            existing.status() != TripStatus.PLANNED
                                    && existing.status() != TripStatus.BOARDING
                    ) {
                        return Mono.error(new BadRequestException(
                                "Only PLANNED or BOARDING trips can be started."
                        ));
                    }

                    Trip updated = copyWithStatus(
                            existing,
                            TripStatus.ACTIVE,
                            existing.actualStartAt() == null
                                    ? Instant.now()
                                    : existing.actualStartAt(),
                            existing.actualEndAt()
                    );

                    return tripRepository.save(updated);
                })
                .flatMap(tripDetailsAssembler::toDetails);
    }

    @Transactional
    public Mono<TripDetailsResponse> endTrip(
            UUID coordinatorId,
            UUID tripId
    ) {
        return getOwnedTrip(coordinatorId, tripId)
                .flatMap(existing -> {
                    if (
                            existing.status() != TripStatus.BOARDING
                                    && existing.status() != TripStatus.ACTIVE
                    ) {
                        return Mono.error(new BadRequestException(
                                "Only BOARDING or ACTIVE trips can be ended."
                        ));
                    }

                    Trip updated = copyWithStatus(
                            existing,
                            TripStatus.COMPLETED,
                            existing.actualStartAt(),
                            Instant.now()
                    );

                    return tripRepository.save(updated);
                })
                .flatMap(tripDetailsAssembler::toDetails);
    }

    @Transactional
    public Mono<TripStopResponse> markStopArrived(
            UUID coordinatorId,
            UUID tripId,
            UUID tripStopId
    ) {
        return getOwnedActiveTripAndStop(coordinatorId, tripId, tripStopId)
                .flatMap(tripStop -> {
                    if (tripStop.status() == TripStopStatus.DEPARTED) {
                        return Mono.error(new BadRequestException(
                                "Departed stop cannot be marked arrived again."
                        ));
                    }

                    TripStop updated = new TripStop(
                            tripStop.id(),
                            tripStop.tripId(),
                            tripStop.stopId(),
                            tripStop.stopSequence(),
                            tripStop.scheduledArrivalAt(),
                            Instant.now(),
                            TripStopStatus.ARRIVED,
                            tripStop.createdAt(),
                            tripStop.updatedAt(),
                            tripStop.version()
                    );

                    return tripStopRepository.save(updated);
                })
                .flatMap(saved -> getTripStopResponse(tripId, saved.id()));
    }

    @Transactional
    public Mono<TripStopResponse> markStopDeparted(
            UUID coordinatorId,
            UUID tripId,
            UUID tripStopId
    ) {
        return getOwnedActiveTripAndStop(coordinatorId, tripId, tripStopId)
                .flatMap(tripStop -> {
                    if (tripStop.status() != TripStopStatus.ARRIVED) {
                        return Mono.error(new BadRequestException(
                                "Only ARRIVED stops can be marked DEPARTED."
                        ));
                    }

                    TripStop updated = new TripStop(
                            tripStop.id(),
                            tripStop.tripId(),
                            tripStop.stopId(),
                            tripStop.stopSequence(),
                            tripStop.scheduledArrivalAt(),
                            tripStop.actualArrivalAt(),
                            TripStopStatus.DEPARTED,
                            tripStop.createdAt(),
                            tripStop.updatedAt(),
                            tripStop.version()
                    );

                    return tripStopRepository.save(updated);
                })
                .flatMap(saved -> getTripStopResponse(tripId, saved.id()));
    }

    @Transactional
    public Mono<TripStopResponse> markStopSkipped(
            UUID coordinatorId,
            UUID tripId,
            UUID tripStopId
    ) {
        return getOwnedActiveTripAndStop(coordinatorId, tripId, tripStopId)
                .flatMap(tripStop -> {
                    if (tripStop.status() == TripStopStatus.DEPARTED) {
                        return Mono.error(new BadRequestException(
                                "Departed stop cannot be skipped."
                        ));
                    }

                    TripStop updated = new TripStop(
                            tripStop.id(),
                            tripStop.tripId(),
                            tripStop.stopId(),
                            tripStop.stopSequence(),
                            tripStop.scheduledArrivalAt(),
                            tripStop.actualArrivalAt(),
                            TripStopStatus.SKIPPED,
                            tripStop.createdAt(),
                            tripStop.updatedAt(),
                            tripStop.version()
                    );

                    return tripStopRepository.save(updated);
                })
                .flatMap(saved -> getTripStopResponse(tripId, saved.id()));
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

    private Mono<TripStop> getOwnedActiveTripAndStop(
            UUID coordinatorId,
            UUID tripId,
            UUID tripStopId
    ) {
        return getOwnedTrip(coordinatorId, tripId)
                .flatMap(trip -> {
                    if (
                            trip.status() != TripStatus.BOARDING
                                    && trip.status() != TripStatus.ACTIVE
                    ) {
                        return Mono.error(new BadRequestException(
                                "Trip must be BOARDING or ACTIVE to update stops."
                        ));
                    }

                    return tripStopRepository
                            .findByIdAndTripId(tripStopId, tripId)
                            .switchIfEmpty(Mono.error(
                                    new NotFoundException(
                                            "Trip stop not found."
                                    )
                            ));
                });
    }

    private Mono<TripStopResponse> getTripStopResponse(
            UUID tripId,
            UUID tripStopId
    ) {
        return tripRepository.findById(tripId)
                .flatMap(tripDetailsAssembler::toDetails)
                .flatMapIterable(TripDetailsResponse::stops)
                .filter(stop -> stop.id().equals(tripStopId))
                .next()
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Trip stop not found.")
                ));
    }

    private Trip copyWithStatus(
            Trip existing,
            TripStatus status,
            Instant actualStartAt,
            Instant actualEndAt
    ) {
        return new Trip(
                existing.id(),
                existing.routeId(),
                existing.busId(),
                existing.coordinatorId(),
                existing.scheduledStartAt(),
                existing.scheduledEndAt(),
                actualStartAt,
                actualEndAt,
                status,
                existing.createdAt(),
                existing.updatedAt(),
                existing.version()
        );
    }
}
