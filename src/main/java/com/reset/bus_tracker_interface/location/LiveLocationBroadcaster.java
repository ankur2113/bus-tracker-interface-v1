package com.reset.bus_tracker_interface.location;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.reset.bus_tracker_interface.location.dto.TripLocationResponse;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
public class LiveLocationBroadcaster {

    private final ConcurrentMap<UUID, Sinks.Many<TripLocationResponse>>
            tripLocationSinks = new ConcurrentHashMap<>();

    public void publish(TripLocationResponse location) {
        Sinks.Many<TripLocationResponse> sink =
                tripLocationSinks.get(location.tripId());

        if (sink == null) {
            return;
        }

        sink.tryEmitNext(location);
    }

    public Flux<TripLocationResponse> stream(UUID tripId) {
        Sinks.Many<TripLocationResponse> sink =
                tripLocationSinks.computeIfAbsent(
                        tripId,
                        id -> Sinks
                                .many()
                                .multicast()
                                .directBestEffort()
                );

        return sink.asFlux();
    }
}
