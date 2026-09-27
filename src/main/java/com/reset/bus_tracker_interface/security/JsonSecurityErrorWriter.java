package com.reset.bus_tracker_interface.security;

import java.time.Instant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.reset.bus_tracker_interface.api.error.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class JsonSecurityErrorWriter {
    private final ObjectMapper objectMapper;

    public JsonSecurityErrorWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Mono<Void> write(
            ServerWebExchange exchange,
            HttpStatus status,
            String code,
            String message
    ) {
        ServerHttpResponse response = exchange.getResponse();

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiError apiError = new ApiError(
                Instant.now(),
                status.value(),
                code,
                message,
                exchange.getRequest().getPath().value()
        );

        try {
            byte[] body = objectMapper.writeValueAsBytes(apiError);

            return response.writeWith(
                    Mono.just(response.bufferFactory().wrap(body))
            );
        } catch (JsonProcessingException exception) {
            return Mono.error(exception);
        }
    }
}
