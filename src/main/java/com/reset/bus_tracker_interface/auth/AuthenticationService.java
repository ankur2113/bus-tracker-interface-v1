package com.reset.bus_tracker_interface.auth;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

import com.reset.bus_tracker_interface.auth.dto.AuthenticatedUserResponse;
import com.reset.bus_tracker_interface.auth.dto.LoginRequest;
import com.reset.bus_tracker_interface.auth.dto.LoginResponse;
import com.reset.bus_tracker_interface.domain.AppUser;
import com.reset.bus_tracker_interface.repository.AppUserRepository;
import com.reset.bus_tracker_interface.security.JwtProperties;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class AuthenticationService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;

    public AuthenticationService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            JwtEncoder jwtEncoder,
            JwtProperties jwtProperties
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.jwtProperties = jwtProperties;
    }

    public Mono<LoginResponse> login(LoginRequest request) {
        String username = request.username()
                .strip()
                .toLowerCase(Locale.ROOT);

        return appUserRepository.findByUsername(username)
                .filter(user -> Boolean.TRUE.equals(user.active()))
                .flatMap(user -> validatePassword(
                        request.password(),
                        user.passwordHash()
                ).filter(Boolean::booleanValue)
                        .map(valid -> createLoginResponse(user)))
                .switchIfEmpty(
                        Mono.error(new InvalidCredentialsException())
                );
    }

    private Mono<Boolean> validatePassword(
            String rawPassword,
            String encodedPassword
    ) {
        return Mono.fromCallable(() ->
                        encodedPassword != null
                                && passwordEncoder.matches(
                                rawPassword,
                                encodedPassword
                        )
                )
                .onErrorReturn(false)
                .subscribeOn(Schedulers.boundedElastic());
    }

    private LoginResponse createLoginResponse(AppUser user) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(
                jwtProperties.accessTokenTtl()
        );

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.issuer())
                .subject(user.id().toString())
                .audience(List.of(jwtProperties.audience()))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("username", user.username())
                .claim("fullName", user.fullName())
                .claim("roles", List.of(user.role().name()))
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .keyId(jwtProperties.keyId())
                .build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();

        AuthenticatedUserResponse authenticatedUser =
                new AuthenticatedUserResponse(
                        user.id(),
                        user.username(),
                        user.fullName(),
                        user.role()
                );

        return new LoginResponse(
                token,
                "Bearer",
                jwtProperties.accessTokenTtl().toSeconds(),
                authenticatedUser
        );
    }
}