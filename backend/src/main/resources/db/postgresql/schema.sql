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
