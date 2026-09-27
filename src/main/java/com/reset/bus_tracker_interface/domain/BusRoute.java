package com.reset.bus_tracker_interface.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("bus_route")
public record BusRoute(@Id UUID id,
                       @Column("route_code") String routeCode,
                       @Column("route_name") String routeName,
                       String description,
                       Boolean active,
                       @ReadOnlyProperty
                       @Column("created_at")
                       Instant createdAt,
                       @ReadOnlyProperty
                       @Column("updated_at")
                       Instant updatedAt,
                       @Version Long version) {
}
