package com.reset.bus_tracker_interface.config;

import java.util.Locale;

import com.reset.bus_tracker_interface.domain.AppUser;
import com.reset.bus_tracker_interface.domain.UserRole;
import com.reset.bus_tracker_interface.repository.AppUserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Configuration
@Profile("local")
@EnableConfigurationProperties(BootstrapAdminProperties.class)
public class LocalAdminBootstrapConfiguration {

    private static final Logger log = LoggerFactory.getLogger(
            LocalAdminBootstrapConfiguration.class
    );

    @Bean
    ApplicationRunner bootstrapLocalAdmin(
            BootstrapAdminProperties properties,
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        return arguments -> {
            if (!properties.enabled()) {
                return;
            }

            String username = properties.username()
                    .strip()
                    .toLowerCase(Locale.ROOT);

            appUserRepository.findByUsername(username)
                    .hasElement()
                    .flatMap(userExists -> {
                        if (userExists) {
                            return Mono.empty();
                        }

                        return Mono.fromCallable(() ->
                                        passwordEncoder.encode(
                                                properties.password()
                                        )
                                )
                                .subscribeOn(Schedulers.boundedElastic())
                                .map(passwordHash -> new AppUser(
                                        null,
                                        username,
                                        properties.fullName(),
                                        properties.email(),
                                        null,
                                        UserRole.ADMIN,
                                        passwordHash,
                                        true,
                                        null,
                                        null,
                                        null
                                ))
                                .flatMap(appUserRepository::save)
                                .doOnSuccess(user -> log.info(
                                        "Local Admin user created: {}",
                                        username
                                ));
                    })
                    .block();
        };
    }
}
