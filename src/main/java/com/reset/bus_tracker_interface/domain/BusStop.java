package com.reset.bus_tracker_interface.domain;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Table("bus_stop")
public record BusStop( @Id UUID id,
                       @Column("stop_code") String stopCode,
                       @Column("stop_name") String stopName,
                       String landmark,
                       BigDecimal latitude,
                       BigDecimal longitude,
                       Boolean active,
                       @ReadOnlyProperty
                       @Column("created_at")
                       Instant createdAt,
                       @ReadOnlyProperty
                       @Column("updated_at")
                       Instant updatedAt,
                       @Version Long version) {
}
