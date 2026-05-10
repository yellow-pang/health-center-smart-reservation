CREATE TABLE IF NOT EXISTS common_code_groups (
    id BIGSERIAL PRIMARY KEY,
    group_code VARCHAR(50) NOT NULL UNIQUE,
    group_name VARCHAR(100) NOT NULL,
    description TEXT,
    system_group BOOLEAN NOT NULL DEFAULT true,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS common_codes (
    id BIGSERIAL PRIMARY KEY,
    group_id BIGINT NOT NULL REFERENCES common_code_groups(id),
    code VARCHAR(50) NOT NULL,
    code_name VARCHAR(100) NOT NULL,
    description TEXT,
    sort_order INTEGER NOT NULL DEFAULT 0,
    system_code BOOLEAN NOT NULL DEFAULT true,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_common_codes_group_code UNIQUE (group_id, code)
);

CREATE INDEX IF NOT EXISTS idx_common_codes_group_active_sort
    ON common_codes (group_id, active, sort_order, code);

CREATE TABLE IF NOT EXISTS health_centers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(30),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS members (
    id BIGSERIAL PRIMARY KEY,
    health_center_id BIGINT REFERENCES health_centers(id),
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    role VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_members_role_active
    ON members (role, active);

CREATE TABLE IF NOT EXISTS service_types (
    id BIGSERIAL PRIMARY KEY,
    health_center_id BIGINT NOT NULL REFERENCES health_centers(id),
    code VARCHAR(50) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    default_capacity INTEGER NOT NULL DEFAULT 5,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_service_types_health_center_code UNIQUE (health_center_id, code)
);

CREATE INDEX IF NOT EXISTS idx_service_types_health_center_active
    ON service_types (health_center_id, active, id);

CREATE TABLE IF NOT EXISTS reservation_slots (
    id BIGSERIAL PRIMARY KEY,
    health_center_id BIGINT NOT NULL REFERENCES health_centers(id),
    service_type_id BIGINT NOT NULL REFERENCES service_types(id),
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    capacity INTEGER NOT NULL DEFAULT 5,
    reserved_count INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_reservation_slots_service_time UNIQUE (service_type_id, slot_date, start_time, end_time),
    CONSTRAINT chk_reservation_slots_capacity CHECK (capacity >= 1),
    CONSTRAINT chk_reservation_slots_reserved_count CHECK (reserved_count >= 0 AND reserved_count <= capacity)
);

CREATE INDEX IF NOT EXISTS idx_reservation_slots_service_date_active
    ON reservation_slots (service_type_id, slot_date, active, start_time);

CREATE TABLE IF NOT EXISTS reservations (
    id BIGSERIAL PRIMARY KEY,
    reservation_no VARCHAR(50) NOT NULL UNIQUE,
    health_center_id BIGINT NOT NULL REFERENCES health_centers(id),
    member_id BIGINT NOT NULL REFERENCES members(id),
    service_type_id BIGINT NOT NULL REFERENCES service_types(id),
    reservation_slot_id BIGINT NOT NULL REFERENCES reservation_slots(id),
    visitor_name VARCHAR(50) NOT NULL,
    visitor_phone VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'RESERVED',
    reserved_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    canceled_at TIMESTAMP,
    checked_in_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_reservations_member_status
    ON reservations (member_id, status);

CREATE INDEX IF NOT EXISTS idx_reservations_slot
    ON reservations (reservation_slot_id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_reservations_member_slot_active
    ON reservations (member_id, reservation_slot_id)
    WHERE status IN ('RESERVED', 'CHECKED_IN');

CREATE TABLE IF NOT EXISTS service_windows (
    id BIGSERIAL PRIMARY KEY,
    health_center_id BIGINT NOT NULL REFERENCES health_centers(id),
    window_number INTEGER NOT NULL,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_service_windows_health_center_number UNIQUE (health_center_id, window_number)
);

CREATE INDEX IF NOT EXISTS idx_service_windows_health_center_active
    ON service_windows (health_center_id, active, window_number);

CREATE TABLE IF NOT EXISTS service_window_service_types (
    id BIGSERIAL PRIMARY KEY,
    service_window_id BIGINT NOT NULL REFERENCES service_windows(id),
    service_type_id BIGINT NOT NULL REFERENCES service_types(id),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_service_window_service_types UNIQUE (service_window_id, service_type_id)
);

CREATE INDEX IF NOT EXISTS idx_service_window_service_types_active
    ON service_window_service_types (service_window_id, active);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES members(id),
    refresh_token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_member_revoked
    ON refresh_tokens (member_id, revoked, expires_at);
