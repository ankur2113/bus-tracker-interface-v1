package com.reset.bus_tracker_interface.location;

import com.reset.bus_tracker_interface.api.error.BadRequestException;
import com.reset.bus_tracker_interface.api.error.NotFoundException;
import com.reset.bus_tracker_interface.domain.Trip;
import com.reset.bus_tracker_interface.domain.TripLocationHistory;
import com.reset.bus_tracker_interface.domain.TripStatus;
import com.reset.bus_tracker_interface.location.dto.TripLocationHistoryResponse;
import com.reset.bus_tracker_interface.location.dto.TripLocationResponse;
import com.reset.bus_tracker_interface.location.dto.UpdateTripLocationRequest;
import com.reset.bus_tracker_interface.repository.CommuterRouteAssignmentRepository;
import com.reset.bus_tracker_interface.repository.TripCurrentLocationRepository;
import com.reset.bus_tracker_interface.repository.TripLocationHistoryRepository;
import com.reset.bus_tracker_interface.repository.TripRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class TripLocationService {
    private static final String LOCATION_SOURCE = "COORDINATOR_APP";

    private final TripRepository tripRepository;
    private final TripCurrentLocationRepository tripCurrentLocationRepository;
    private final TripLocationHistoryRepository tripLocationHistoryRepository;
    private final DatabaseClient databaseClient;
    private final CommuterRouteAssignmentRepository assignmentRepository;

    private final LiveLocationBroadcaster liveLocationBroadcaster;

    public TripLocationService(
            TripRepository tripRepository,
            TripCurrentLocationRepository tripCurrentLocationRepository,
            TripLocationHistoryRepository tripLocationHistoryRepository,
            DatabaseClient databaseClient,
            CommuterRouteAssignmentRepository assignmentRepository,
            LiveLocationBroadcaster liveLocationBroadcaster
    ) {
        this.tripRepository = tripRepository;
        this.tripCurrentLocationRepository = tripCurrentLocationRepository;
        this.tripLocationHistoryRepository = tripLocationHistoryRepository;
        this.databaseClient = databaseClient;
        this.assignmentRepository = assignmentRepository;
        this.liveLocationBroadcaster = liveLocationBroadcaster;
    }

    @Transactional
    public Mono<TripLocationResponse> updateCoordinatorLocation(
            UUID coordinatorId,
            UUID tripId,
            UpdateTripLocationRequest request
    ) {
        return getOwnedLiveTrip(coordinatorId, tripId)
                .flatMap(trip -> {
                    Instant receivedAt = Instant.now();
                    Instant recordedAt = request.locationRecordedAt() == null
                            ? receivedAt
                            : request.locationRecordedAt();

                    TripLocationHistory history = new TripLocationHistory(
                            null,
                            trip.id(),
                            request.latitude(),
                            request.longitude(),
                            request.accuracyMeters(),
                            request.speedMps(),
                            request.headingDegrees(),
                            recordedAt,
                            receivedAt,
                            LOCATION_SOURCE
                    );

                    TripLocationResponse response = new TripLocationResponse(
                            trip.id(),
                            request.latitude(),
                            request.longitude(),
                            request.accuracyMeters(),
                            request.speedMps(),
                            request.headingDegrees(),
                            recordedAt,
                            receivedAt
                    );

                    return tripLocationHistoryRepository.save(history)
                            .then(upsertCurrentLocation(
                                    trip.id(),
                                    request,
                                    recordedAt,
                                    receivedAt
                            ))
                            .thenReturn(response)
                            .doOnSuccess(liveLocationBroadcaster::publish);
                });
    }

    public Mono<TripLocationResponse> getLatestLocationForCoordinator(
            UUID coordinatorId,
            UUID tripId
    ) {
        return getOwnedTrip(coordinatorId, tripId)
                .then(getLatestLocation(tripId));
    }

    public Flux<TripLocationHistoryResponse> getLocationHistoryForCoordinator(
            UUID coordinatorId,
            UUID tripId
    ) {
        return getOwnedTrip(coordinatorId, tripId)
                .thenMany(tripLocationHistoryRepository
                        .findTop100ByTripIdOrderByLocationRecordedAtDesc(
                                tripId
                        )
                )
                .map(TripLocationMapper::toHistoryResponse);
    }

    public Mono<TripLocationResponse> getLatestLocationForCommuter(
            UUID commuterId,
            UUID tripId
    ) {
        return tripRepository.findById(tripId)
                .filter(this::isLiveTrip)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Live trip not found.")
                ))
                .flatMap(trip -> assignmentRepository
                        .findByCommuterIdAndRouteId(
                                commuterId,
                                trip.routeId()
                        )
                        .filter(assignment ->
                                Boolean.TRUE.equals(assignment.active())
                        )
                        .switchIfEmpty(Mono.error(
                                new NotFoundException(
                                        "Trip not found for this commuter."
                                )
                        ))
                        .then(getLatestLocation(tripId))
                );
    }

    private Mono<TripLocationResponse> getLatestLocation(UUID tripId) {
        return tripCurrentLocationRepository.findByTripId(tripId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException(
                                "Location not available for this trip yet."
                        )
                ))
                .map(TripLocationMapper::toCurrentLocationResponse);
    }

    private Mono<Trip> getOwnedLiveTrip(UUID coordinatorId, UUID tripId) {
        return getOwnedTrip(coordinatorId, tripId)
                .flatMap(trip -> {
                    if (!isLiveTrip(trip)) {
                        return Mono.error(new BadRequestException(
                                "Location can be updated only for BOARDING or ACTIVE trips."
                        ));
                    }

                    return Mono.just(trip);
                });
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

    private boolean isLiveTrip(Trip trip) {
        return trip.status() == TripStatus.BOARDING
                || trip.status() == TripStatus.ACTIVE;
    }

    private Mono<Void> upsertCurrentLocation(
            UUID tripId,
            UpdateTripLocationRequest request,
            Instant recordedAt,
            Instant receivedAt
    ) {
        String sql = """
                INSERT INTO trip_current_location (
                    trip_id,
                    latitude,
                    longitude,
                    accuracy_meters,
                    speed_mps,
                    heading_degrees,
                    location_recorded_at,
                    location_received_at,
                    updated_at
                )
                VALUES (
                    :tripId,
                    :latitude,
                    :longitude,
                    :accuracyMeters,
                    :speedMps,
                    :headingDegrees,
                    :recordedAt,
                    :receivedAt,
                    CURRENT_TIMESTAMP
                )
                ON CONFLICT (trip_id)
                DO UPDATE SET
                    latitude = EXCLUDED.latitude,
                    longitude = EXCLUDED.longitude,
                    accuracy_meters = EXCLUDED.accuracy_meters,
                    speed_mps = EXCLUDED.speed_mps,
                    heading_degrees = EXCLUDED.heading_degrees,
                    location_recorded_at = EXCLUDED.location_recorded_at,
                    location_received_at = EXCLUDED.location_received_at,
                    updated_at = CURRENT_TIMESTAMP
                """;

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql)
                .bind("tripId", tripId)
                .bind("latitude", request.latitude())
                .bind("longitude", request.longitude())
                .bind("recordedAt", recordedAt)
                .bind("receivedAt", receivedAt);

        spec = bindNullable(
                spec,
                "accuracyMeters",
                request.accuracyMeters(),
                BigDecimal.class
        );

        spec = bindNullable(
                spec,
                "speedMps",
                request.speedMps(),
                BigDecimal.class
        );

        spec = bindNullable(
                spec,
                "headingDegrees",
                request.headingDegrees(),
                BigDecimal.class
        );

        return spec.fetch()
                .rowsUpdated()
                .then();
    }

    private DatabaseClient.GenericExecuteSpec bindNullable(
            DatabaseClient.GenericExecuteSpec spec,
            String name,
            Object value,
            Class<?> type
    ) {
        if (value == null) {
            return spec.bindNull(name, type);
        }

        return spec.bind(name, value);
    }

    public Flux<TripLocationResponse> streamLocationForCoordinator(
            UUID coordinatorId,
            UUID tripId
    ) {
        return getOwnedTrip(coordinatorId, tripId)
                .thenMany(locationStream(tripId));
    }

    public Flux<TripLocationResponse> streamLocationForCommuter(
            UUID commuterId,
            UUID tripId
    ) {
        return tripRepository.findById(tripId)
                .filter(this::isLiveTrip)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Live trip not found.")
                ))
                .flatMap(trip -> assignmentRepository
                        .findByCommuterIdAndRouteId(
                                commuterId,
                                trip.routeId()
                        )
                        .filter(assignment ->
                                Boolean.TRUE.equals(assignment.active())
                        )
                        .switchIfEmpty(Mono.error(
                                new NotFoundException(
                                        "Trip not found for this commuter."
                                )
                        ))
                        .thenReturn(trip)
                )
                .thenMany(locationStream(tripId));
    }

    private Flux<TripLocationResponse> locationStream(UUID tripId) {
        Flux<TripLocationResponse> latestLocation =
                getLatestLocation(tripId)
                        .flux()
                        .onErrorResume(
                                NotFoundException.class,
                                exception -> Flux.empty()
                        );

        Flux<TripLocationResponse> liveUpdates =
                liveLocationBroadcaster.stream(tripId);

        return latestLocation.concatWith(liveUpdates);
    }
}
