package com.reset.bus_tracker_interface.auth;

import java.util.List;

import com.reset.bus_tracker_interface.auth.dto.CurrentUserResponse;
import com.reset.bus_tracker_interface.auth.dto.LoginRequest;
import com.reset.bus_tracker_interface.auth.dto.LoginResponse;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(
            AuthenticationService authenticationService
    ) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return authenticationService.login(request)
                .map(ResponseEntity::ok);
    }

    @GetMapping("/me")
    public Mono<CurrentUserResponse> getCurrentUser(
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<String> roles = jwt.getClaimAsStringList("roles");

        return Mono.just(new CurrentUserResponse(
                jwt.getSubject(),
                jwt.getClaimAsString("username"),
                jwt.getClaimAsString("fullName"),
                roles == null ? List.of() : roles
        ));
    }
}
