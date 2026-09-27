package com.reset.bus_tracker_interface.progress;

import com.reset.bus_tracker_interface.api.error.NotFoundException;
import com.reset.bus_tracker_interface.domain.BusStop;
import com.reset.bus_tracker_interface.domain.CheckInStatus;
import com.reset.bus_tracker_interface.domain.CommuterRouteAssignment;
import com.reset.bus_tracker_interface.domain.Trip;
import com.reset.bus_tracker_interface.domain.TripStatus;
import com.reset.bus_tracker_interface.domain.TripStop;
import com.reset.bus_tracker_interface.domain.TripStopStatus;
import com.reset.bus_tracker_interface.progress.dto.TripProgressResponse;
import com.reset.bus_tracker_interface.repository.BusStopRepository;
import com.reset.bus_tracker_interface.repository.CommuterCheckInRepository;
import com.reset.bus_tracker_interface.repository.CommuterRouteAssignmentRepository;
import com.reset.bus_tracker_interface.repository.TripRepository;
import com.reset.bus_tracker_interface.repository.TripStopRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TripProgressService {
    private static final List<TripStatus> VISIBLE_STATUSES = List.of(
            TripStatus.PLANNED,
            TripStatus.BOARDING,
            TripStatus.ACTIVE
    );

    private final TripRepository tripRepository;
    private final TripStopRepository tripStopRepository;
    private final BusStopRepository busStopRepository;
    private final CommuterRouteAssignmentRepository assignmentRepository;
    private final CommuterCheckInRepository checkInRepository;

    public TripProgressService(
            TripRepository tripRepository,
            TripStopRepository tripStopRepository,
            BusStopRepository busStopRepository,
            CommuterRouteAssignmentRepository assignmentRepository,
            CommuterCheckInRepository checkInRepository
    ) {
        this.tripRepository = tripRepository;
        this.tripStopRepository = tripStopRepository;
        this.busStopRepository = busStopRepository;
        this.assignmentRepository = assignmentRepository;
        this.checkInRepository = checkInRepository;
    }

    public Mono<TripProgressResponse> getProgress(UUID commuterId, UUID tripId) {
        return getVisibleTrip(commuterId, tripId)
                .flatMap(trip -> tripStopRepository
                        .findByTripIdOrderByStopSequence(trip.id())
                        .collectList()
                        .flatMap(stops -> buildProgress(commuterId, trip, stops))
                );
    }

    private Mono<TripProgressResponse> buildProgress(
            UUID commuterId,
            Trip trip,
            List<TripStop> stops
    ) {
        if (stops.isEmpty()) {
            return Mono.error(new NotFoundException("Trip stops not found."));
        }

        return resolveTargetStop(commuterId, trip, stops)
                .flatMap(targetTripStop -> {
                    TripStop currentTripStop = resolveCurrentStop(stops);
                    int currentSequence = currentTripStop == null
                            ? 0
                            : currentTripStop.stopSequence();
                    int stopsAway = Math.max(
                            targetTripStop.stopSequence() - currentSequence,
                            0
                    );
                    long etaMinutes = estimateMinutes(targetTripStop);
                    String alertLevel = alertLevel(stopsAway, etaMinutes);
                    String message = alertMessage(stopsAway, etaMinutes);

                    Mono<Optional<BusStop>> targetStop = busStopRepository
                            .findById(targetTripStop.stopId())
                            .map(Optional::of)
                            .defaultIfEmpty(Optional.empty());

                    Mono<Optional<BusStop>> currentStop = currentTripStop == null
                            ? Mono.just(Optional.empty())
                            : busStopRepository
                                    .findById(currentTripStop.stopId())
                                    .map(Optional::of)
                                    .defaultIfEmpty(Optional.empty());

                    return Mono.zip(targetStop, currentStop)
                            .map(tuple -> new TripProgressResponse(
                                    trip.id(),
                                    targetTripStop.id(),
                                    targetTripStop.stopId(),
                                    tuple.getT1().map(BusStop::stopName).orElse(null),
                                    targetTripStop.stopSequence(),
                                    currentTripStop == null ? null : currentTripStop.id(),
                                    tuple.getT2().map(BusStop::stopName).orElse(null),
                                    currentTripStop == null ? null : currentTripStop.stopSequence(),
                                    stopsAway,
                                    etaMinutes,
                                    alertLevel,
                                    message,
                                    targetTripStop.scheduledArrivalAt()
                            ));
                });
    }

    private Mono<TripStop> resolveTargetStop(
            UUID commuterId,
            Trip trip,
            List<TripStop> stops
    ) {
        Mono<TripStop> fromActiveCheckIn = checkInRepository
                .findByTripIdAndCommuterId(trip.id(), commuterId)
                .filter(checkIn -> checkIn.status() != CheckInStatus.CANCELLED)
                .flatMap(checkIn -> tripStopRepository.findByIdAndTripId(
                        checkIn.tripStopId(),
                        trip.id()
                ));

        Mono<TripStop> fromAssignment = assignmentRepository
                .findByCommuterIdAndRouteId(commuterId, trip.routeId())
                .filter(CommuterRouteAssignment::active)
                .flatMap(assignment -> {
                    if (assignment.defaultStopId() == null) {
                        return Mono.empty();
                    }

                    return stops.stream()
                            .filter(stop -> stop.stopId().equals(assignment.defaultStopId()))
                            .findFirst()
                            .map(Mono::just)
                            .orElseGet(Mono::empty);
                });

        return fromActiveCheckIn
                .switchIfEmpty(fromAssignment)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "Pickup stop not found for this commuter."
                )));
    }

    private TripStop resolveCurrentStop(List<TripStop> stops) {
        return stops.stream()
                .filter(stop -> stop.status() == TripStopStatus.ARRIVED
                        || stop.status() == TripStopStatus.DEPARTED
                        || stop.status() == TripStopStatus.SKIPPED)
                .max(Comparator.comparing(TripStop::stopSequence))
                .orElse(null);
    }

    private long estimateMinutes(TripStop targetTripStop) {
        if (targetTripStop.scheduledArrivalAt() == null) {
            return -1L;
        }

        long minutes = Duration.between(
                Instant.now(),
                targetTripStop.scheduledArrivalAt()
        ).toMinutes();

        return Math.max(minutes, 0L);
    }

    private String alertLevel(int stopsAway, long etaMinutes) {
        if (stopsAway <= 0) {
            return "ARRIVING";
        }
        if (stopsAway <= 2) {
            return "TWO_STOPS_AWAY";
        }
        if (stopsAway <= 5) {
            return "FIVE_STOPS_AWAY";
        }
        if (etaMinutes >= 0 && etaMinutes <= 10) {
            return "TEN_MINUTES_AWAY";
        }
        return "ON_THE_WAY";
    }

    private String alertMessage(int stopsAway, long etaMinutes) {
        if (stopsAway <= 0) {
            return "Bus is arriving at your stop.";
        }
        if (stopsAway <= 2) {
            return "Bus is about 2 stops away.";
        }
        if (stopsAway <= 5) {
            return "Bus is about 5 stops away.";
        }
        if (etaMinutes >= 0 && etaMinutes <= 10) {
            return "Your bus is about 10 minutes away.";
        }
        return "Bus is on the way.";
    }

    private Mono<Trip> getVisibleTrip(UUID commuterId, UUID tripId) {
        return tripRepository.findById(tripId)
                .filter(trip -> VISIBLE_STATUSES.contains(trip.status()))
                .flatMap(trip -> assignmentRepository
                        .findByCommuterIdAndRouteId(commuterId, trip.routeId())
                        .filter(CommuterRouteAssignment::active)
                        .switchIfEmpty(Mono.error(new NotFoundException(
                                "Trip not found for this commuter."
                        )))
                        .thenReturn(trip)
                )
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "Visible trip not found."
                )));
    }
}
