package com.reset.bus_tracker_interface.checkin;

import com.reset.bus_tracker_interface.api.error.NotFoundException;
import com.reset.bus_tracker_interface.checkin.dto.CheckInResponse;
import com.reset.bus_tracker_interface.domain.AppUser;
import com.reset.bus_tracker_interface.domain.BusStop;
import com.reset.bus_tracker_interface.domain.CommuterCheckIn;
import com.reset.bus_tracker_interface.domain.TripStop;
import com.reset.bus_tracker_interface.repository.AppUserRepository;
import com.reset.bus_tracker_interface.repository.BusStopRepository;
import com.reset.bus_tracker_interface.repository.TripStopRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CheckInAssembler {

    private final AppUserRepository appUserRepository;
    private final TripStopRepository tripStopRepository;
    private final BusStopRepository busStopRepository;

    public CheckInAssembler(
            AppUserRepository appUserRepository,
            TripStopRepository tripStopRepository,
            BusStopRepository busStopRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.tripStopRepository = tripStopRepository;
        this.busStopRepository = busStopRepository;
    }

    public Mono<CheckInResponse> toResponse(CommuterCheckIn checkIn) {
        Mono<AppUser> commuterMono =
                appUserRepository.findById(checkIn.commuterId())
                        .switchIfEmpty(Mono.error(
                                new NotFoundException("Commuter not found.")
                        ));

        Mono<TripStop> tripStopMono =
                tripStopRepository.findById(checkIn.tripStopId())
                        .switchIfEmpty(Mono.error(
                                new NotFoundException("Trip stop not found.")
                        ));

        return Mono.zip(commuterMono, tripStopMono)
                .flatMap(tuple -> {
                    AppUser commuter = tuple.getT1();
                    TripStop tripStop = tuple.getT2();

                    return busStopRepository.findById(tripStop.stopId())
                            .switchIfEmpty(Mono.error(
                                    new NotFoundException("Stop not found.")
                            ))
                            .map(stop -> toResponse(
                                    checkIn,
                                    commuter,
                                    tripStop,
                                    stop
                            ));
                });
    }

    private CheckInResponse toResponse(
            CommuterCheckIn checkIn,
            AppUser commuter,
            TripStop tripStop,
            BusStop stop
    ) {
        return new CheckInResponse(
                checkIn.id(),

                checkIn.tripId(),
                checkIn.tripStopId(),

                commuter.id(),
                commuter.fullName(),
                commuter.phone(),

                stop.id(),
                stop.stopCode(),
                stop.stopName(),
                tripStop.stopSequence(),

                checkIn.status(),

                checkIn.checkedInAt(),
                checkIn.boardedAt(),
                checkIn.cancelledAt()
        );
    }
}