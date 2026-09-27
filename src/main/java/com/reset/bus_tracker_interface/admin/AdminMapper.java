package com.reset.bus_tracker_interface.admin;

import com.reset.bus_tracker_interface.admin.dto.BusResponse;
import com.reset.bus_tracker_interface.admin.dto.RouteResponse;
import com.reset.bus_tracker_interface.admin.dto.StopResponse;
import com.reset.bus_tracker_interface.admin.dto.UserResponse;
import com.reset.bus_tracker_interface.domain.AppUser;
import com.reset.bus_tracker_interface.domain.Bus;
import com.reset.bus_tracker_interface.domain.BusRoute;
import com.reset.bus_tracker_interface.domain.BusStop;

public final class AdminMapper {

    private AdminMapper() {
    }

    public static UserResponse toUserResponse(AppUser user) {
        return new UserResponse(
                user.id(),
                user.username(),
                user.fullName(),
                user.email(),
                user.phone(),
                user.role(),
                user.active(),
                user.createdAt(),
                user.updatedAt()
        );
    }

    public static BusResponse toBusResponse(Bus bus) {
        return new BusResponse(
                bus.id(),
                bus.registrationNumber(),
                bus.displayName(),
                bus.capacity(),
                bus.active(),
                bus.createdAt(),
                bus.updatedAt()
        );
    }

    public static StopResponse toStopResponse(BusStop stop) {
        return new StopResponse(
                stop.id(),
                stop.stopCode(),
                stop.stopName(),
                stop.landmark(),
                stop.latitude(),
                stop.longitude(),
                stop.active(),
                stop.createdAt(),
                stop.updatedAt()
        );
    }

    public static RouteResponse toRouteResponse(BusRoute route) {
        return new RouteResponse(
                route.id(),
                route.routeCode(),
                route.routeName(),
                route.description(),
                route.active(),
                route.createdAt(),
                route.updatedAt()
        );
    }
}
