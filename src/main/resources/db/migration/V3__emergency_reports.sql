CREATE TABLE emergency_report (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id UUID NOT NULL,
    reported_by_user_id UUID NOT NULL,
    emergency_type VARCHAR(60) NOT NULL,
    message VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    reported_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMPTZ,
    resolved_by_user_id UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_emergency_trip FOREIGN KEY (trip_id) REFERENCES trip(id) ON DELETE CASCADE,
    CONSTRAINT fk_emergency_reported_by FOREIGN KEY (reported_by_user_id) REFERENCES app_user(id),
    CONSTRAINT fk_emergency_resolved_by FOREIGN KEY (resolved_by_user_id) REFERENCES app_user(id),
    CONSTRAINT ck_emergency_status CHECK (status IN ('OPEN', 'RESOLVED'))
);

CREATE INDEX idx_emergency_report_status_reported_at
    ON emergency_report (status, reported_at DESC);

CREATE INDEX idx_emergency_report_trip
    ON emergency_report (trip_id, reported_at DESC);

CREATE TRIGGER trg_emergency_report_updated_at
BEFORE UPDATE ON emergency_report
FOR EACH ROW EXECUTE FUNCTION set_updated_at();
