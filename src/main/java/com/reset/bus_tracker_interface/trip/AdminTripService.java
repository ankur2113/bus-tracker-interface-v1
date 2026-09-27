package com.reset.bus_tracker_interface.trip;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.reset.bus_tracker_interface.api.error.BadRequestException;
import com.reset.bus_tracker_interface.api.error.ConflictException;
import com.reset.bus_tracker_interface.api.error.NotFoundException;
import com.reset.bus_tracker_interface.domain.AppUser;
import com.reset.bus_tracker_interface.domain.Bus;
import com.reset.bus_tracker_interface.domain.BusRoute;
import com.reset.bus_tracker_interface.domain.RouteStop;
import com.reset.bus_tracker_interface.domain.Trip;
import com.reset.bus_tracker_interface.domain.TripStatus;
import com.reset.bus_tracker_interface.domain.TripStop;
import com.reset.bus_tracker_interface.domain.TripStopStatus;
import com.reset.bus_tracker_interface.domain.UserRole;
import com.reset.bus_tracker_interface.repository.AppUserRepository;
import com.reset.bus_tracker_interface.repository.BusRepository;
import com.reset.bus_tracker_interface.repository.BusRouteRepository;
import com.reset.bus_tracker_interface.repository.RouteStopRepository;
import com.reset.bus_tracker_interface.repository.TripRepository;
import com.reset.bus_tracker_interface.repository.TripStopRepository;
import com.reset.bus_tracker_interface.trip.dto.CreateTripRequest;
import com.reset.bus_tracker_interface.trip.dto.TripDetailsResponse;
import com.reset.bus_tracker_interface.trip.dto.TripResponse;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AdminTripService {

    private static final List<TripStatus> LIVE_STATUSES =
            List.of(TripStatus.BOARDING, TripStatus.ACTIVE);

    private final TripRepository tripRepository;
    private final TripStopRepository tripStopRepository;
    private final RouteStopRepository routeStopRepository;
    private final BusRouteRepository busRouteRepository;
    private final BusRepository busRepository;
    private final AppUserRepository appUserRepository;
    private final TripDetailsAssembler tripDetailsAssembler;

    public AdminTripService(
            TripRepository tripRepository,
            TripStopRepository tripStopRepository,
            RouteStopRepository routeStopRepository,
            BusRouteRepository busRouteRepository,
            BusRepository busRepository,
            AppUserRepository appUserRepository,
            TripDetailsAssembler tripDetailsAssembler
    ) {
        this.tripRepository = tripRepository;
        this.tripStopRepository = tripStopRepository;
        this.routeStopRepository = routeStopRepository;
        this.busRouteRepository = busRouteRepository;
        this.busRepository = busRepository;
        this.appUserRepository = appUserRepository;
        this.tripDetailsAssembler = tripDetailsAssembler;
    }

    public Flux<TripResponse> listTrips() {
        return tripRepository.findAll()
                .flatMap(tripDetailsAssembler::toResponse);
    }

    public Mono<TripDetailsResponse> getTripDetails(UUID tripId) {
        return tripRepository.findById(tripId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Trip not found.")
                ))
                .flatMap(tripDetailsAssembler::toDetails);
    }

    @Transactional
    public Mono<TripDetailsResponse> createTrip(CreateTripRequest request) {
        validateSchedule(request);

        Mono<BusRoute> routeMono = busRouteRepository.findById(request.routeId())
                .filter(route -> Boolean.TRUE.equals(route.active()))
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Active route not found.")
                ));

        Mono<Bus> busMono = busRepository.findById(request.busId())
                .filter(bus -> Boolean.TRUE.equals(bus.active()))
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Active bus not found.")
                ));

        Mono<AppUser> coordinatorMono =
                appUserRepository.findById(request.coordinatorId())
                        .filter(user ->
                                Boolean.TRUE.equals(user.active())
                                        && user.role() == UserRole.COORDINATOR
                        )
                        .switchIfEmpty(Mono.error(
                                new NotFoundException(
                                        "Active coordinator not found."
                                )
                        ));

        Mono<List<RouteStop>> routeStopsMono =
                routeStopRepository
                        .findByRouteIdOrderByStopSequence(request.routeId())
                        .collectList()
                        .filter(routeStops -> !routeStops.isEmpty())
                        .switchIfEmpty(Mono.error(
                                new BadRequestException(
                                        "Route must have at least one stop before creating a trip."
                                )
                        ));

        Mono<Boolean> liveBusCheck = tripRepository
                .findFirstByBusIdAndStatusIn(request.busId(), LIVE_STATUSES)
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ConflictException(
                                "Bus already has a live trip."
                        ));
                    }

                    return Mono.just(true);
                });

        Mono<Boolean> liveCoordinatorCheck = tripRepository
                .findFirstByCoordinatorIdAndStatusIn(
                        request.coordinatorId(),
                        LIVE_STATUSES
                )
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ConflictException(
                                "Coordinator already has a live trip."
                        ));
                    }

                    return Mono.just(true);
                });

        return Mono.zip(
                        routeMono,
                        busMono,
                        coordinatorMono,
                        routeStopsMono,
                        liveBusCheck,
                        liveCoordinatorCheck
                )
                .flatMap(tuple -> {
                    List<RouteStop> routeStops = tuple.getT4();

                    Trip trip = new Trip(
                            null,
                            request.routeId(),
                            request.busId(),
                            request.coordinatorId(),
                            request.scheduledStartAt(),
                            request.scheduledEndAt(),
                            null,
                            null,
                            TripStatus.PLANNED,
                            null,
                            null,
                            null
                    );

                    return tripRepository.save(trip)
                            .flatMap(savedTrip ->
                                    createTripStopsSnapshot(
                                            savedTrip,
                                            routeStops
                                    )
                                            .thenReturn(savedTrip)
                            );
                })
                .flatMap(tripDetailsAssembler::toDetails);
    }

    @Transactional
    public Mono<TripDetailsResponse> cancelTrip(UUID tripId) {
        return tripRepository.findById(tripId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Trip not found.")
                ))
                .flatMap(existing -> {
                    if (existing.status() == TripStatus.COMPLETED) {
                        return Mono.error(new BadRequestException(
                                "Completed trip cannot be cancelled."
                        ));
                    }

                    Trip cancelled = new Trip(
                            existing.id(),
                            existing.routeId(),
                            existing.busId(),
                            existing.coordinatorId(),
                            existing.scheduledStartAt(),
                            existing.scheduledEndAt(),
                            existing.actualStartAt(),
                            existing.actualEndAt(),
                            TripStatus.CANCELLED,
                            existing.createdAt(),
                            existing.updatedAt(),
                            existing.version()
                    );

                    return tripRepository.save(cancelled);
                })
                .flatMap(tripDetailsAssembler::toDetails);
    }

    private Mono<Void> createTripStopsSnapshot(
            Trip trip,
            List<RouteStop> routeStops
    ) {
        return Flux.fromIterable(routeStops)
                .concatMap(routeStop -> {
                    Instant scheduledArrivalAt = null;

                    if (routeStop.plannedArrivalOffsetMinutes() != null) {
                        scheduledArrivalAt = trip.scheduledStartAt()
                                .plus(Duration.ofMinutes(
                                        routeStop.plannedArrivalOffsetMinutes()
                                ));
                    }

                    TripStop tripStop = new TripStop(
                            null,
                            trip.id(),
                            routeStop.stopId(),
                            routeStop.stopSequence(),
                            scheduledArrivalAt,
                            null,
                            TripStopStatus.PENDING,
                            null,
                            null,
                            null
                    );

                    return tripStopRepository.save(tripStop);
                })
                .then();
    }

    private void validateSchedule(CreateTripRequest request) {
        if (
                request.scheduledEndAt() != null
                        && request.scheduledEndAt().isBefore(request.scheduledStartAt())
        ) {
            throw new BadRequestException(
                    "Scheduled end time must be after scheduled start time."
            );
        }
    }
}
