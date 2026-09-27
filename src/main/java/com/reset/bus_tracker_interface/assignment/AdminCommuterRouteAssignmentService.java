package com.reset.bus_tracker_interface.assignment;

import com.reset.bus_tracker_interface.api.error.ConflictException;
import com.reset.bus_tracker_interface.api.error.NotFoundException;
import com.reset.bus_tracker_interface.assignment.dto.AssignCommuterRouteRequest;
import com.reset.bus_tracker_interface.assignment.dto.CommuterRouteAssignmentResponse;
import com.reset.bus_tracker_interface.domain.CommuterRouteAssignment;
import com.reset.bus_tracker_interface.domain.UserRole;
import com.reset.bus_tracker_interface.repository.AppUserRepository;
import com.reset.bus_tracker_interface.repository.BusRouteRepository;
import com.reset.bus_tracker_interface.repository.CommuterRouteAssignmentRepository;
import com.reset.bus_tracker_interface.repository.RouteStopRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class AdminCommuterRouteAssignmentService {

    private final CommuterRouteAssignmentRepository assignmentRepository;
    private final AppUserRepository appUserRepository;
    private final BusRouteRepository busRouteRepository;
    private final RouteStopRepository routeStopRepository;

    public AdminCommuterRouteAssignmentService(
            CommuterRouteAssignmentRepository assignmentRepository,
            AppUserRepository appUserRepository,
            BusRouteRepository busRouteRepository,
            RouteStopRepository routeStopRepository
    ) {
        this.assignmentRepository = assignmentRepository;
        this.appUserRepository = appUserRepository;
        this.busRouteRepository = busRouteRepository;
        this.routeStopRepository = routeStopRepository;
    }

    public Flux<CommuterRouteAssignmentResponse> listAssignments() {
        return assignmentRepository.findAll()
                .map(this::toResponse);
    }

    public Flux<CommuterRouteAssignmentResponse> listAssignmentsForCommuter(
            UUID commuterId
    ) {
        return assignmentRepository.findByCommuterIdAndActiveTrue(commuterId)
                .map(this::toResponse);
    }

    public Mono<CommuterRouteAssignmentResponse> assignCommuterToRoute(
            AssignCommuterRouteRequest request
    ) {
        return validateCommuter(request.commuterId())
                .then(validateRoute(request.routeId()))
                .then(validateDefaultStopBelongsToRoute(
                        request.routeId(),
                        request.defaultStopId()
                ))
                .then(assignmentRepository.findByCommuterIdAndRouteId(
                        request.commuterId(),
                        request.routeId()
                ))
                .flatMap(existing -> {
                    if (Boolean.TRUE.equals(existing.active())) {
                        return Mono.error(new ConflictException(
                                "Commuter is already assigned to this route."
                        ));
                    }

                    CommuterRouteAssignment reactivated =
                            new CommuterRouteAssignment(
                                    existing.id(),
                                    existing.commuterId(),
                                    existing.routeId(),
                                    request.defaultStopId(),
                                    true,
                                    existing.createdAt(),
                                    existing.updatedAt(),
                                    existing.version()
                            );

                    return assignmentRepository.save(reactivated);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    CommuterRouteAssignment assignment =
                            new CommuterRouteAssignment(
                                    null,
                                    request.commuterId(),
                                    request.routeId(),
                                    request.defaultStopId(),
                                    true,
                                    null,
                                    null,
                                    null
                            );

                    return assignmentRepository.save(assignment);
                }))
                .map(this::toResponse);
    }

    public Mono<Void> deactivateAssignment(UUID assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Assignment not found.")
                ))
                .flatMap(existing -> {
                    CommuterRouteAssignment updated =
                            new CommuterRouteAssignment(
                                    existing.id(),
                                    existing.commuterId(),
                                    existing.routeId(),
                                    existing.defaultStopId(),
                                    false,
                                    existing.createdAt(),
                                    existing.updatedAt(),
                                    existing.version()
                            );

                    return assignmentRepository.save(updated);
                })
                .then();
    }

    private Mono<Void> validateCommuter(UUID commuterId) {
        return appUserRepository.findById(commuterId)
                .filter(user ->
                        Boolean.TRUE.equals(user.active())
                                && user.role() == UserRole.COMMUTER
                )
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Active commuter not found.")
                ))
                .then();
    }

    private Mono<Void> validateRoute(UUID routeId) {
        return busRouteRepository.findById(routeId)
                .filter(route -> Boolean.TRUE.equals(route.active()))
                .switchIfEmpty(Mono.error(
                        new NotFoundException("Active route not found.")
                ))
                .then();
    }

    private Mono<Void> validateDefaultStopBelongsToRoute(
            UUID routeId,
            UUID defaultStopId
    ) {
        if (defaultStopId == null) {
            return Mono.empty();
        }

        return routeStopRepository.findByRouteIdAndStopId(
                        routeId,
                        defaultStopId
                )
                .switchIfEmpty(Mono.error(
                        new NotFoundException(
                                "Default stop does not belong to this route."
                        )
                ))
                .then();
    }

    private CommuterRouteAssignmentResponse toResponse(
            CommuterRouteAssignment assignment
    ) {
        return new CommuterRouteAssignmentResponse(
                assignment.id(),
                assignment.commuterId(),
                assignment.routeId(),
                assignment.defaultStopId(),
                assignment.active(),
                assignment.createdAt(),
                assignment.updatedAt()
        );
    }
}