package com.reset.bus_tracker_interface.trip;

import java.util.List;
import java.util.UUID;

import com.reset.bus_tracker_interface.api.error.NotFoundException;
import com.reset.bus_tracker_interface.domain.Trip;
import com.reset.bus_tracker_interface.domain.TripStatus;
import com.reset.bus_tracker_interface.repository.CommuterRouteAssignmentRepository;
import com.reset.bus_tracker_interface.repository.TripRepository;
import com.reset.bus_tracker_interface.trip.dto.TripDetailsResponse;
import com.reset.bus_tracker_interface.trip.dto.TripResponse;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CommuterTripService {

    private static final List<TripStatus> VISIBLE_TRIP_STATUSES =
            List.of(
                    TripStatus.PLANNED,
                    TripStatus.BOARDING,
                    TripStatus.ACTIVE
            );

    private final TripRepository tripRepository;
    private final CommuterRouteAssignmentRepository assignmentRepository;
    private final TripDetailsAssembler tripDetailsAssembler;

    public CommuterTripService(
            TripRepository tripRepository,
            CommuterRouteAssignmentRepository assignmentRepository,
            TripDetailsAssembler tripDetailsAssembler
    ) {
        this.tripRepository = tripRepository;
        this.assignmentRepository = assignmentRepository;
        this.tripDetailsAssembler = tripDetailsAssembler;
    }

    public Flux<TripResponse> listVisibleTrips(UUID commuterId) {
        return assignmentRepository
                .findByCommuterIdAndActiveTrue(commuterId)
                .map(assignment -> assignment.routeId())
                .collectList()
                .flatMapMany(routeIds -> {
                    if (routeIds.isEmpty()) {
                        return Flux.empty();
                    }

                    return tripRepository.findByRouteIdInAndStatusIn(
                            routeIds,
                            VISIBLE_TRIP_STATUSES
                    );
                })
                .flatMap(tripDetailsAssembler::toResponse);
    }

    public Mono<TripDetailsResponse> getVisibleTripDetails(
            UUID commuterId,
            UUID tripId
    ) {
        return getVisibleTrip(commuterId, tripId)
                .flatMap(tripDetailsAssembler::toDetails);
    }

    public Mono<Trip> getVisibleTrip(UUID commuterId, UUID tripId) {
        return tripRepository.findById(tripId)
                .filter(trip -> VISIBLE_TRIP_STATUSES.contains(trip.status()))
                .flatMap(trip -> assignmentRepository
                        .findByCommuterIdAndRouteId(
                                commuterId,
                                trip.routeId()
                        )
                        .filter(assignment ->
                                Boolean.TRUE.equals(assignment.active())
                        )
                        .thenReturn(trip)
                )
                .switchIfEmpty(Mono.error(
                        new NotFoundException(
                                "Trip not found for this commuter."
                        )
                ));
    }
}