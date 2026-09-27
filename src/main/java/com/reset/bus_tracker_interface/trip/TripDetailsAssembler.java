package com.reset.bus_tracker_interface.trip;

import com.reset.bus_tracker_interface.api.error.NotFoundException;
import com.reset.bus_tracker_interface.domain.*;
import com.reset.bus_tracker_interface.repository.*;
import com.reset.bus_tracker_interface.trip.dto.TripDetailsResponse;
import com.reset.bus_tracker_interface.trip.dto.TripResponse;
import com.reset.bus_tracker_interface.trip.dto.TripStopResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Comparator;

@Component
public class TripDetailsAssembler {

    private final BusRouteRepository busRouteRepository;
    private final BusRepository busRepository;
    private final AppUserRepository appUserRepository;
    private final BusStopRepository busStopRepository;
    private final TripStopRepository tripStopRepository;

    public TripDetailsAssembler(
            BusRouteRepository busRouteRepository,
            BusRepository busRepository,
            AppUserRepository appUserRepository,
            BusStopRepository busStopRepository,
            TripStopRepository tripStopRepository
    ) {
        this.busRouteRepository = busRouteRepository;
        this.busRepository = busRepository;
        this.appUserRepository = appUserRepository;
        this.busStopRepository = busStopRepository;
        this.tripStopRepository = tripStopRepository;
    }

    public Mono<TripDetailsResponse> toDetails(Trip trip) {
        Mono<TripResponse> tripResponseMono = toResponse(trip);

        Mono<java.util.List<TripStopResponse>> stopsMono =
                tripStopRepository.findByTripIdOrderByStopSequence(trip.id())
                        .flatMap(this::toStopResponse)
                        .sort(Comparator.comparing(
                                TripStopResponse::stopSequence
                        ))
                        .collectList();

        return Mono.zip(tripResponseMono, stopsMono)
                .map(tuple -> new TripDetailsResponse(
                        tuple.getT1(),
                        tuple.getT2()
                ));
    }

    public Mono<TripResponse> toResponse(Trip trip) {
        Mono<BusRoute> routeMono = busRouteRepository.findById(trip.routeId())
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Route not found for trip.")
                ));

        Mono<Bus> busMono = busRepository.findById(trip.busId())
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Bus not found for trip.")
                ));

        Mono<AppUser> coordinatorMono =
                appUserRepository.findById(trip.coordinatorId())
                        .switchIfEmpty(Mono.error(
                                new NotFoundException(
                                        "Coordinator not found for trip."
                                )
                        ));

        return Mono.zip(routeMono, busMono, coordinatorMono)
                .map(tuple -> {
                    BusRoute route = tuple.getT1();
                    Bus bus = tuple.getT2();
                    AppUser coordinator = tuple.getT3();

                    return new TripResponse(
                            trip.id(),

                            route.id(),
                            route.routeCode(),
                            route.routeName(),

                            bus.id(),
                            bus.registrationNumber(),
                            bus.displayName(),

                            coordinator.id(),
                            coordinator.fullName(),

                            trip.scheduledStartAt(),
                            trip.scheduledEndAt(),
                            trip.actualStartAt(),
                            trip.actualEndAt(),

                            trip.status()
                    );
                });
    }

    private Mono<TripStopResponse> toStopResponse(TripStop tripStop) {
        return busStopRepository.findById(tripStop.stopId())
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Stop not found for trip stop.")
                ))
                .map(stop -> toStopResponse(tripStop, stop));
    }

    private TripStopResponse toStopResponse(
            TripStop tripStop,
            BusStop stop
    ) {
        return new TripStopResponse(
                tripStop.id(),
                stop.id(),

                stop.stopCode(),
                stop.stopName(),
                stop.landmark(),

                stop.latitude(),
                stop.longitude(),

                tripStop.stopSequence(),

                tripStop.scheduledArrivalAt(),
                tripStop.actualArrivalAt(),

                tripStop.status()
        );
    }
}