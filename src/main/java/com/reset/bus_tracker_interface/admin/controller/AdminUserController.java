package com.reset.bus_tracker_interface.admin.controller;

import java.net.URI;
import java.util.UUID;

import com.reset.bus_tracker_interface.admin.dto.CreateUserRequest;
import com.reset.bus_tracker_interface.admin.dto.UpdateUserRequest;
import com.reset.bus_tracker_interface.admin.dto.UserResponse;
import com.reset.bus_tracker_interface.admin.service.AdminUserService;
import com.reset.bus_tracker_interface.domain.UserRole;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public Flux<UserResponse> listUsers(
            @RequestParam(required = false) UserRole role
    ) {
        return adminUserService.listUsers(role);
    }

    @GetMapping("/{userId}")
    public Mono<UserResponse> getUser(@PathVariable UUID userId) {
        return adminUserService.getUser(userId);
    }

    @PostMapping
    public Mono<ResponseEntity<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        return adminUserService.createUser(request)
                .map(response -> ResponseEntity
                        .created(URI.create(
                                "/api/v1/admin/users/" + response.id()
                        ))
                        .body(response));
    }

    @PutMapping("/{userId}")
    public Mono<UserResponse> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return adminUserService.updateUser(userId, request);
    }

    @DeleteMapping("/{userId}")
    public Mono<ResponseEntity<Void>> deactivateUser(
            @PathVariable UUID userId
    ) {
        return adminUserService.deactivateUser(userId)
                .thenReturn(ResponseEntity.noContent().build());
    }
}
