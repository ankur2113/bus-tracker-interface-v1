CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


-- =========================================================
-- USERS
-- =========================================================

CREATE TABLE app_user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    username VARCHAR(60) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(25),

    role VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255),

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_app_user_username UNIQUE (username),
    CONSTRAINT ck_app_user_role
        CHECK (role IN ('ADMIN', 'COORDINATOR', 'COMMUTER'))
);

CREATE UNIQUE INDEX ux_app_user_email_lower
    ON app_user (LOWER(email))
    WHERE email IS NOT NULL;

CREATE UNIQUE INDEX ux_app_user_phone
    ON app_user (phone)
    WHERE phone IS NOT NULL;


-- =========================================================
-- BUSES
-- =========================================================

CREATE TABLE bus (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    registration_number VARCHAR(40) NOT NULL,
    display_name VARCHAR(100),
    capacity INTEGER NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_bus_registration_number UNIQUE (registration_number),
    CONSTRAINT ck_bus_capacity CHECK (capacity > 0)
);


-- =========================================================
-- ROUTES AND STOPS
-- =========================================================

CREATE TABLE bus_route (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    route_code VARCHAR(40) NOT NULL,
    route_name VARCHAR(150) NOT NULL,
    description VARCHAR(500),

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_bus_route_code UNIQUE (route_code)
);

CREATE TABLE bus_stop (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    stop_code VARCHAR(40),
    stop_name VARCHAR(150) NOT NULL,
    landmark VARCHAR(255),

    latitude NUMERIC(9, 6) NOT NULL,
    longitude NUMERIC(9, 6) NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT uq_bus_stop_code UNIQUE (stop_code),
    CONSTRAINT ck_bus_stop_latitude CHECK (latitude BETWEEN -90 AND 90),
    CONSTRAINT ck_bus_stop_longitude CHECK (longitude BETWEEN -180 AND 180)
);

CREATE TABLE route_stop (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    route_id UUID NOT NULL,
    stop_id UUID NOT NULL,

    stop_sequence INTEGER NOT NULL,
    planned_arrival_offset_minutes INTEGER,

    pickup_enabled BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_route_stop_route
        FOREIGN KEY (route_id) REFERENCES bus_route(id),

    CONSTRAINT fk_route_stop_stop
        FOREIGN KEY (stop_id) REFERENCES bus_stop(id),

    CONSTRAINT uq_route_stop_sequence
        UNIQUE (route_id, stop_sequence),

    CONSTRAINT uq_route_stop_unique_stop
        UNIQUE (route_id, stop_id),

    CONSTRAINT ck_route_stop_sequence
        CHECK (stop_sequence > 0),

    CONSTRAINT ck_route_stop_offset
        CHECK (
            planned_arrival_offset_minutes IS NULL
            OR planned_arrival_offset_minutes >= 0
        )
);


-- =========================================================
-- COMMUTER-ROUTE ASSIGNMENT
-- =========================================================

CREATE TABLE commuter_route_assignment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    commuter_id UUID NOT NULL,
    route_id UUID NOT NULL,
    default_stop_id UUID,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_commuter_route_assignment_commuter
        FOREIGN KEY (commuter_id) REFERENCES app_user(id),

    CONSTRAINT fk_commuter_route_assignment_route
        FOREIGN KEY (route_id) REFERENCES bus_route(id),

    CONSTRAINT fk_commuter_route_assignment_stop
        FOREIGN KEY (default_stop_id) REFERENCES bus_stop(id),

    CONSTRAINT uq_commuter_route_assignment
        UNIQUE (commuter_id, route_id)
);


-- =========================================================
-- TRIPS
-- =========================================================

CREATE TABLE trip (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    route_id UUID NOT NULL,
    bus_id UUID NOT NULL,
    coordinator_id UUID NOT NULL,

    scheduled_start_at TIMESTAMPTZ NOT NULL,
    scheduled_end_at TIMESTAMPTZ,

    actual_start_at TIMESTAMPTZ,
    actual_end_at TIMESTAMPTZ,

    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED',

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_trip_route
        FOREIGN KEY (route_id) REFERENCES bus_route(id),

    CONSTRAINT fk_trip_bus
        FOREIGN KEY (bus_id) REFERENCES bus(id),

    CONSTRAINT fk_trip_coordinator
        FOREIGN KEY (coordinator_id) REFERENCES app_user(id),

    CONSTRAINT ck_trip_status
        CHECK (status IN ('PLANNED', 'BOARDING', 'ACTIVE', 'COMPLETED', 'CANCELLED')),

    CONSTRAINT ck_trip_scheduled_times
        CHECK (
            scheduled_end_at IS NULL
            OR scheduled_end_at >= scheduled_start_at
        ),

    CONSTRAINT ck_trip_actual_times
        CHECK (
            actual_end_at IS NULL
            OR actual_start_at IS NULL
            OR actual_end_at >= actual_start_at
        )
);

CREATE INDEX idx_trip_route_status
    ON trip (route_id, status);

CREATE INDEX idx_trip_coordinator_status
    ON trip (coordinator_id, status);

CREATE INDEX idx_trip_scheduled_start
    ON trip (scheduled_start_at);

CREATE UNIQUE INDEX ux_trip_live_coordinator
    ON trip (coordinator_id)
    WHERE status IN ('BOARDING', 'ACTIVE');

CREATE UNIQUE INDEX ux_trip_live_bus
    ON trip (bus_id)
    WHERE status IN ('BOARDING', 'ACTIVE');


-- =========================================================
-- SNAPSHOT OF STOPS FOR A SPECIFIC TRIP
-- =========================================================

CREATE TABLE trip_stop (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    trip_id UUID NOT NULL,
    stop_id UUID NOT NULL,

    stop_sequence INTEGER NOT NULL,
    scheduled_arrival_at TIMESTAMPTZ,
    actual_arrival_at TIMESTAMPTZ,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_trip_stop_trip
        FOREIGN KEY (trip_id) REFERENCES trip(id) ON DELETE CASCADE,

    CONSTRAINT fk_trip_stop_stop
        FOREIGN KEY (stop_id) REFERENCES bus_stop(id),

    CONSTRAINT uq_trip_stop_sequence
        UNIQUE (trip_id, stop_sequence),

    CONSTRAINT uq_trip_stop_trip_id_id
        UNIQUE (trip_id, id),

    CONSTRAINT ck_trip_stop_sequence
        CHECK (stop_sequence > 0),

    CONSTRAINT ck_trip_stop_status
        CHECK (status IN ('PENDING', 'ARRIVED', 'DEPARTED', 'SKIPPED'))
);

CREATE INDEX idx_trip_stop_trip_sequence
    ON trip_stop (trip_id, stop_sequence);


-- =========================================================
-- COMMUTER CHECK-INS
-- =========================================================

CREATE TABLE commuter_check_in (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    trip_id UUID NOT NULL,
    trip_stop_id UUID NOT NULL,
    commuter_id UUID NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'CHECKED_IN',

    checked_in_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    boarded_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,

    CONSTRAINT fk_check_in_trip
        FOREIGN KEY (trip_id) REFERENCES trip(id) ON DELETE CASCADE,

    CONSTRAINT fk_check_in_trip_stop
        FOREIGN KEY (trip_id, trip_stop_id)
        REFERENCES trip_stop (trip_id, id)
        ON DELETE CASCADE,

    CONSTRAINT fk_check_in_commuter
        FOREIGN KEY (commuter_id) REFERENCES app_user(id),

    CONSTRAINT uq_check_in_one_per_commuter_trip
        UNIQUE (trip_id, commuter_id),

    CONSTRAINT ck_check_in_status
        CHECK (status IN ('CHECKED_IN', 'CANCELLED', 'BOARDED', 'NO_SHOW'))
);

CREATE INDEX idx_check_in_trip_stop_status
    ON commuter_check_in (trip_id, trip_stop_id, status);

CREATE INDEX idx_check_in_commuter
    ON commuter_check_in (commuter_id);


-- =========================================================
-- GPS LOCATION
-- =========================================================

CREATE TABLE trip_current_location (
    trip_id UUID PRIMARY KEY,

    latitude NUMERIC(9, 6) NOT NULL,
    longitude NUMERIC(9, 6) NOT NULL,

    accuracy_meters NUMERIC(8, 2),
    speed_mps NUMERIC(8, 2),
    heading_degrees NUMERIC(6, 2),

    location_recorded_at TIMESTAMPTZ NOT NULL,
    location_received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_trip_current_location_trip
        FOREIGN KEY (trip_id) REFERENCES trip(id) ON DELETE CASCADE,

    CONSTRAINT ck_current_location_latitude
        CHECK (latitude BETWEEN -90 AND 90),

    CONSTRAINT ck_current_location_longitude
        CHECK (longitude BETWEEN -180 AND 180),

    CONSTRAINT ck_current_location_accuracy
        CHECK (accuracy_meters IS NULL OR accuracy_meters >= 0),

    CONSTRAINT ck_current_location_speed
        CHECK (speed_mps IS NULL OR speed_mps >= 0),

    CONSTRAINT ck_current_location_heading
        CHECK (
            heading_degrees IS NULL
            OR heading_degrees BETWEEN 0 AND 360
        )
);

CREATE TABLE trip_location_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    trip_id UUID NOT NULL,

    latitude NUMERIC(9, 6) NOT NULL,
    longitude NUMERIC(9, 6) NOT NULL,

    accuracy_meters NUMERIC(8, 2),
    speed_mps NUMERIC(8, 2),
    heading_degrees NUMERIC(6, 2),

    location_recorded_at TIMESTAMPTZ NOT NULL,
    location_received_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    source VARCHAR(30) NOT NULL DEFAULT 'COORDINATOR_APP',

    CONSTRAINT fk_trip_location_history_trip
        FOREIGN KEY (trip_id) REFERENCES trip(id) ON DELETE CASCADE,

    CONSTRAINT ck_location_history_latitude
        CHECK (latitude BETWEEN -90 AND 90),

    CONSTRAINT ck_location_history_longitude
        CHECK (longitude BETWEEN -180 AND 180),

    CONSTRAINT ck_location_history_accuracy
        CHECK (accuracy_meters IS NULL OR accuracy_meters >= 0),

    CONSTRAINT ck_location_history_speed
        CHECK (speed_mps IS NULL OR speed_mps >= 0),

    CONSTRAINT ck_location_history_heading
        CHECK (
            heading_degrees IS NULL
            OR heading_degrees BETWEEN 0 AND 360
        )
);

CREATE INDEX idx_location_history_trip_recorded_at
    ON trip_location_history (trip_id, location_recorded_at DESC);


-- =========================================================
-- UPDATED-AT TRIGGERS
-- =========================================================

CREATE TRIGGER trg_app_user_updated_at
BEFORE UPDATE ON app_user
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_bus_updated_at
BEFORE UPDATE ON bus
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_bus_route_updated_at
BEFORE UPDATE ON bus_route
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_bus_stop_updated_at
BEFORE UPDATE ON bus_stop
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_route_stop_updated_at
BEFORE UPDATE ON route_stop
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_commuter_route_assignment_updated_at
BEFORE UPDATE ON commuter_route_assignment
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_trip_updated_at
BEFORE UPDATE ON trip
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_trip_stop_updated_at
BEFORE UPDATE ON trip_stop
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_commuter_check_in_updated_at
BEFORE UPDATE ON commuter_check_in
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_trip_current_location_updated_at
BEFORE UPDATE ON trip_current_location
FOR EACH ROW EXECUTE FUNCTION set_updated_at();