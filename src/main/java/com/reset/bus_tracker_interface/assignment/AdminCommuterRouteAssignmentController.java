package com.reset.bus_tracker_interface.assignment;

import java.net.URI;
import java.util.UUID;

import com.reset.bus_tracker_interface.assignment.dto.AssignCommuterRouteRequest;
import com.reset.bus_tracker_interface.assignment.dto.CommuterRouteAssignmentResponse;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/admin/commuter-route-assignments")
public class AdminCommuterRouteAssignmentController {

    private final AdminCommuterRouteAssignmentService assignmentService;

    public AdminCommuterRouteAssignmentController(
            AdminCommuterRouteAssignmentService assignmentService
    ) {
        this.assignmentService = assignmentService;
    }

    @GetMapping
    public Flux<CommuterRouteAssignmentResponse> listAssignments() {
        return assignmentService.listAssignments();
    }

    @GetMapping("/commuters/{commuterId}")
    public Flux<CommuterRouteAssignmentResponse> listAssignmentsForCommuter(
            @PathVariable UUID commuterId
    ) {
        return assignmentService.listAssignmentsForCommuter(commuterId);
    }

    @PostMapping
    public Mono<ResponseEntity<CommuterRouteAssignmentResponse>> assign(
            @Valid @RequestBody AssignCommuterRouteRequest request
    ) {
        return assignmentService.assignCommuterToRoute(request)
                .map(response -> ResponseEntity
                        .created(URI.create(
                                "/api/v1/admin/commuter-route-assignments/"
                                        + response.id()
                        ))
                        .body(response));
    }

    @DeleteMapping("/{assignmentId}")
    public Mono<ResponseEntity<Void>> deactivateAssignment(
            @PathVariable UUID assignmentId
    ) {
        return assignmentService.deactivateAssignment(assignmentId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}