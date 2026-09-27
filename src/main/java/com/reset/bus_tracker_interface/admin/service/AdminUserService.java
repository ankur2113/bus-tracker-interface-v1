package com.reset.bus_tracker_interface.admin.service;

import java.util.Locale;
import java.util.UUID;

import com.reset.bus_tracker_interface.admin.AdminMapper;
import com.reset.bus_tracker_interface.admin.dto.CreateUserRequest;
import com.reset.bus_tracker_interface.admin.dto.UpdateUserRequest;
import com.reset.bus_tracker_interface.admin.dto.UserResponse;
import com.reset.bus_tracker_interface.api.error.BadRequestException;
import com.reset.bus_tracker_interface.api.error.ConflictException;
import com.reset.bus_tracker_interface.api.error.NotFoundException;
import com.reset.bus_tracker_interface.domain.AppUser;
import com.reset.bus_tracker_interface.domain.UserRole;
import com.reset.bus_tracker_interface.repository.AppUserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class AdminUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Flux<UserResponse> listUsers(UserRole role) {
        Flux<AppUser> users = role == null
                ? appUserRepository.findAll()
                : appUserRepository.findByRole(role);

        return users.map(AdminMapper::toUserResponse);
    }

    public Mono<UserResponse> getUser(UUID userId) {
        return appUserRepository.findById(userId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("User not found.")
                ))
                .map(AdminMapper::toUserResponse);
    }

    public Mono<UserResponse> createUser(CreateUserRequest request) {
        validateManagedRole(request.role());

        String username = normalizeUsername(request.username());

        return appUserRepository.findByUsername(username)
                .hasElement()
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ConflictException(
                                "Username already exists."
                        ));
                    }

                    return encodePassword(request.password())
                            .map(passwordHash -> new AppUser(
                                    null,
                                    username,
                                    request.fullName(),
                                    normalizeNullable(request.email()),
                                    normalizeNullable(request.phone()),
                                    request.role(),
                                    passwordHash,
                                    true,
                                    null,
                                    null,
                                    null
                            ))
                            .flatMap(appUserRepository::save)
                            .map(AdminMapper::toUserResponse);
                });
    }

    public Mono<UserResponse> updateUser(
            UUID userId,
            UpdateUserRequest request
    ) {
        return appUserRepository.findById(userId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("User not found.")
                ))
                .flatMap(existing -> {
                    AppUser updated = new AppUser(
                            existing.id(),
                            existing.username(),
                            request.fullName(),
                            normalizeNullable(request.email()),
                            normalizeNullable(request.phone()),
                            existing.role(),
                            existing.passwordHash(),
                            request.active() == null
                                    ? existing.active()
                                    : request.active(),
                            existing.createdAt(),
                            existing.updatedAt(),
                            existing.version()
                    );

                    return appUserRepository.save(updated);
                })
                .map(AdminMapper::toUserResponse);
    }

    public Mono<Void> deactivateUser(UUID userId) {
        return appUserRepository.findById(userId)
                .switchIfEmpty(Mono.error(
                        new NotFoundException("User not found.")
                ))
                .flatMap(existing -> {
                    AppUser updated = new AppUser(
                            existing.id(),
                            existing.username(),
                            existing.fullName(),
                            existing.email(),
                            existing.phone(),
                            existing.role(),
                            existing.passwordHash(),
                            false,
                            existing.createdAt(),
                            existing.updatedAt(),
                            existing.version()
                    );

                    return appUserRepository.save(updated);
                })
                .then();
    }

    private Mono<String> encodePassword(String rawPassword) {
        return Mono.fromCallable(() -> passwordEncoder.encode(rawPassword))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private void validateManagedRole(UserRole role) {
        if (role == UserRole.ADMIN) {
            throw new BadRequestException(
                    "Creating ADMIN users from this endpoint is disabled."
            );
        }
    }

    private String normalizeUsername(String username) {
        return username.strip().toLowerCase(Locale.ROOT);
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.strip();
    }
}
