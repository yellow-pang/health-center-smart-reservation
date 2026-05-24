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

CREATE TABLE IF NOT EXISTS social_accounts (
    id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES members(id),
    provider VARCHAR(30) NOT NULL,
    provider_user_id VARCHAR(100) NOT NULL,
    provider_email VARCHAR(100),
    linked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_social_accounts_provider_user UNIQUE (provider, provider_user_id)
);

CREATE INDEX IF NOT EXISTS idx_social_accounts_member
    ON social_accounts (member_id);

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

CREATE TABLE IF NOT EXISTS visits (
    id BIGSERIAL PRIMARY KEY,
    health_center_id BIGINT NOT NULL REFERENCES health_centers(id),
    reservation_id BIGINT REFERENCES reservations(id),
    service_type_id BIGINT NOT NULL REFERENCES service_types(id),
    member_id BIGINT REFERENCES members(id),
    registered_by BIGINT REFERENCES members(id),
    visitor_name VARCHAR(50),
    visitor_phone VARCHAR(30),
    visit_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'WAITING',
    checked_in_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_visits_reservation
    ON visits (reservation_id)
    WHERE reservation_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_visits_health_center_checked_in
    ON visits (health_center_id, checked_in_at);

CREATE TABLE IF NOT EXISTS queue_ticket_counters (
    id BIGSERIAL PRIMARY KEY,
    health_center_id BIGINT NOT NULL REFERENCES health_centers(id),
    service_type_id BIGINT NOT NULL REFERENCES service_types(id),
    issued_date DATE NOT NULL,
    last_ticket_number INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_queue_ticket_counters_daily UNIQUE (health_center_id, service_type_id, issued_date),
    CONSTRAINT chk_queue_ticket_counters_last_number CHECK (last_ticket_number >= 0)
);

CREATE INDEX IF NOT EXISTS idx_queue_ticket_counters_daily
    ON queue_ticket_counters (health_center_id, service_type_id, issued_date);

CREATE TABLE IF NOT EXISTS queue_tickets (
    id BIGSERIAL PRIMARY KEY,
    health_center_id BIGINT NOT NULL REFERENCES health_centers(id),
    visit_id BIGINT NOT NULL REFERENCES visits(id),
    service_type_id BIGINT NOT NULL REFERENCES service_types(id),
    ticket_number INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'WAITING',
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    called_at TIMESTAMP,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    hold_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_queue_tickets_health_center_service_status
    ON queue_tickets (health_center_id, service_type_id, status);

CREATE INDEX IF NOT EXISTS idx_queue_tickets_issued_at
    ON queue_tickets (issued_at);

CREATE INDEX IF NOT EXISTS idx_queue_tickets_list_lookup
    ON queue_tickets (health_center_id, service_type_id, status, issued_at, ticket_number);

CREATE UNIQUE INDEX IF NOT EXISTS uk_queue_tickets_daily_ticket_number
    ON queue_tickets (health_center_id, service_type_id, (issued_at::date), ticket_number);

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

ALTER TABLE service_windows
    ADD COLUMN IF NOT EXISTS staff_id BIGINT REFERENCES members(id);

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
