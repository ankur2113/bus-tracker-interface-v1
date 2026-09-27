package com.reset.bus_tracker_interface.repository;

import com.reset.bus_tracker_interface.domain.AppUser;
import com.reset.bus_tracker_interface.domain.UserRole;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AppUserRepository extends ReactiveCrudRepository<AppUser, UUID> {
    Mono<AppUser> findByUsername(String username);
    Mono<AppUser> findByEmailIgnoreCase(String email);
    Flux<AppUser> findByRole(UserRole role);
}
