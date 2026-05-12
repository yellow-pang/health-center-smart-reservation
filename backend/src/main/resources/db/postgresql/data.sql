INSERT INTO common_code_groups (group_code, group_name, description, system_group, active)
VALUES
    ('USER_ROLE', '사용자 역할', '시스템 사용자 역할 코드', true, true),
    ('RESERVATION_STATUS', '예약 상태', '예약 진행 상태 코드', true, true),
    ('VISIT_TYPE', '방문 유형', '예약 방문, 현장 방문, 직원 대리 접수 구분', true, true),
    ('VISIT_STATUS', '방문 상태', '방문 접수 및 처리 상태 코드', true, true),
    ('QUEUE_STATUS', '대기 상태', '대기번호 처리 상태 코드', true, true),
    ('CONGESTION_LEVEL', '혼잡도', '현재 혼잡도 표시 코드', true, true)
ON CONFLICT (group_code) DO UPDATE
SET group_name = EXCLUDED.group_name,
    description = EXCLUDED.description,
    system_group = EXCLUDED.system_group,
    active = EXCLUDED.active,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO common_codes (group_id, code, code_name, description, sort_order, system_code, active)
SELECT common_code_groups.id,
       seed.code,
       seed.code_name,
       seed.description,
       seed.sort_order,
       true,
       true
FROM common_code_groups
CROSS JOIN (
    VALUES
        ('USER_ROLE', 'CITIZEN', '일반 시민', '본인 예약 사용자', 1),
        ('USER_ROLE', 'GUARDIAN', '보호자', '가족 또는 고령층 대리 예약 사용자', 2),
        ('USER_ROLE', 'STAFF', '직원', '보건소 현장 업무 처리자', 3),
        ('USER_ROLE', 'ADMIN', '관리자', '보건소 운영 관리자', 4),
        ('RESERVATION_STATUS', 'RESERVED', '예약 완료', '사용자가 예약을 완료한 상태', 1),
        ('RESERVATION_STATUS', 'CANCELED', '예약 취소', '예약이 취소된 상태', 2),
        ('RESERVATION_STATUS', 'CHECKED_IN', '체크인 완료', '방문 당일 체크인이 완료된 상태', 3),
        ('RESERVATION_STATUS', 'NO_SHOW', '미방문', '예약자가 정해진 시간 내 방문하지 않은 상태', 4),
        ('RESERVATION_STATUS', 'COMPLETED', '처리 완료', '예약 기반 업무가 완료된 상태', 5),
        ('VISIT_TYPE', 'RESERVED', '예약 방문', '사전 예약 후 방문', 1),
        ('VISIT_TYPE', 'WALK_IN', '현장 방문', '예약 없이 현장 접수', 2),
        ('VISIT_TYPE', 'STAFF_PROXY', '직원 대리 접수', '전화 또는 현장 요청을 직원이 대신 접수', 3),
        ('VISIT_STATUS', 'REGISTERED', '접수 완료', '방문 접수가 완료된 상태', 1),
        ('VISIT_STATUS', 'WAITING', '대기중', '대기번호가 발급되어 대기 중인 상태', 2),
        ('VISIT_STATUS', 'IN_PROGRESS', '처리중', '창구에서 처리 중인 상태', 3),
        ('VISIT_STATUS', 'COMPLETED', '완료', '방문 업무가 완료된 상태', 4),
        ('VISIT_STATUS', 'CANCELED', '취소', '방문 접수가 취소된 상태', 5),
        ('QUEUE_STATUS', 'WAITING', '대기중', '대기열에 등록된 상태', 1),
        ('QUEUE_STATUS', 'CALLED', '호출됨', '직원이 호출한 상태', 2),
        ('QUEUE_STATUS', 'IN_PROGRESS', '처리중', '업무 처리 중인 상태', 3),
        ('QUEUE_STATUS', 'HOLD', '보류', '호출 후 응답하지 않아 보류된 상태', 4),
        ('QUEUE_STATUS', 'COMPLETED', '완료', '업무 처리가 완료된 상태', 5),
        ('QUEUE_STATUS', 'CANCELED', '취소', '대기 등록이 취소된 상태', 6),
        ('QUEUE_STATUS', 'NO_SHOW', '미방문', '최종 미응답 또는 미방문 상태', 7),
        ('CONGESTION_LEVEL', 'LOW', '여유', '대기 인원과 예상 대기시간이 낮은 상태', 1),
        ('CONGESTION_LEVEL', 'NORMAL', '보통', '일반적인 대기 상태', 2),
        ('CONGESTION_LEVEL', 'HIGH', '혼잡', '대기 인원 또는 예상 대기시간이 높은 상태', 3)
) AS seed(group_code, code, code_name, description, sort_order)
WHERE common_code_groups.group_code = seed.group_code
ON CONFLICT (group_id, code) DO UPDATE
SET code_name = EXCLUDED.code_name,
    description = EXCLUDED.description,
    sort_order = EXCLUDED.sort_order,
    system_code = EXCLUDED.system_code,
    active = EXCLUDED.active,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO health_centers (id, name, address, phone, active)
VALUES (1, '기본 보건소', '서울특별시 중구 세종대로 110', '02-120', true)
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name,
    address = EXCLUDED.address,
    phone = EXCLUDED.phone,
    active = EXCLUDED.active,
    updated_at = CURRENT_TIMESTAMP;

SELECT setval(
    pg_get_serial_sequence('health_centers', 'id'),
    GREATEST((SELECT MAX(id) FROM health_centers), 1)
);

INSERT INTO members (health_center_id, email, password, name, phone, role, active)
VALUES
    (1, 'admin@test.com', '82czkZUHGnH2zTnhrlm5AX7SL+FhUf2zdsXi7fqSC3E=', '보건소 관리자', '010-0000-0001', 'ADMIN', true),
    (1, 'staff@test.com', 'R/JdIngCdK3yEw4hXqIMVzaaQlsvQfvZgQFKcv4qX9w=', '보건소 직원', '010-0000-0002', 'STAFF', true),
    (NULL, 'citizen@test.com', 'wJT8jOG2GleMvpVinUOb9UOy2yQyXwPtExZBcjXo3s0=', '일반 시민', '010-0000-0003', 'CITIZEN', true),
    (NULL, 'guardian@test.com', 'nf3TOXMkBnH5xCqH5ZwomKoQrSzNYZDrZNOlMUFcBZo=', '보호자', '010-0000-0004', 'GUARDIAN', true)
ON CONFLICT (email) DO UPDATE
SET health_center_id = EXCLUDED.health_center_id,
    password = EXCLUDED.password,
    name = EXCLUDED.name,
    phone = EXCLUDED.phone,
    role = EXCLUDED.role,
    active = EXCLUDED.active,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO service_types (health_center_id, code, name, description, default_capacity, active)
VALUES
    (1, 'VACCINATION', '예방접종', '예방접종 예약 및 현장 접수', 5, true),
    (1, 'HEALTH_CHECK', '건강검진/검사', '건강검진과 검사 예약 및 현장 접수', 5, true),
    (1, 'HEALTH_CONSULT', '건강상담', '건강 상담 예약 및 현장 접수', 5, true)
ON CONFLICT (health_center_id, code) DO UPDATE
SET name = EXCLUDED.name,
    description = EXCLUDED.description,
    default_capacity = EXCLUDED.default_capacity,
    active = EXCLUDED.active,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO reservation_slots (
    health_center_id,
    service_type_id,
    slot_date,
    start_time,
    end_time,
    capacity,
    reserved_count,
    active
)
SELECT
    st.health_center_id,
    st.id,
    slot_days.slot_date::date,
    slot_times.start_time::time,
    (slot_times.start_time + INTERVAL '30 minutes')::time,
    st.default_capacity,
    0,
    true
FROM service_types st
CROSS JOIN generate_series(CURRENT_DATE, CURRENT_DATE + INTERVAL '14 days', INTERVAL '1 day') AS slot_days(slot_date)
CROSS JOIN (
    VALUES
        (TIME '09:00'),
        (TIME '09:30'),
        (TIME '10:00'),
        (TIME '10:30'),
        (TIME '11:00'),
        (TIME '11:30'),
        (TIME '13:00'),
        (TIME '13:30'),
        (TIME '14:00'),
        (TIME '14:30'),
        (TIME '15:00'),
        (TIME '15:30'),
        (TIME '16:00'),
        (TIME '16:30')
) AS slot_times(start_time)
WHERE st.health_center_id = 1
  AND st.active = true
ON CONFLICT (service_type_id, slot_date, start_time, end_time) DO UPDATE
SET capacity = EXCLUDED.capacity,
    active = EXCLUDED.active,
    updated_at = CURRENT_TIMESTAMP;

WITH swagger_reservation_seed AS (
    SELECT *
    FROM (
        VALUES
            ('RSV-SWAGGER-CHECKIN-001', 'VACCINATION', 1, TIME '09:00', 'Swagger체크인', '010-1234-5678'),
            ('RSV-SWAGGER-CANCEL-001', 'VACCINATION', 1, TIME '09:30', 'Swagger취소', '010-2345-6789'),
            ('RSV-SWAGGER-DETAIL-001', 'HEALTH_CHECK', 1, TIME '10:00', 'Swagger상세', '010-3456-7890')
    ) AS seed(reservation_no, service_code, slot_day_offset, start_time, visitor_name, visitor_phone)
),
swagger_member AS (
    SELECT id AS member_id
    FROM members
    WHERE email = 'citizen@test.com'
),
swagger_target_reservation AS (
    SELECT seed.reservation_no,
           rs.health_center_id,
           swagger_member.member_id,
           rs.service_type_id,
           rs.id AS reservation_slot_id,
           seed.visitor_name,
           seed.visitor_phone
    FROM swagger_reservation_seed seed
    INNER JOIN service_types st
        ON st.code = seed.service_code
       AND st.health_center_id = 1
    INNER JOIN reservation_slots rs
        ON rs.service_type_id = st.id
       AND rs.slot_date = CURRENT_DATE + seed.slot_day_offset
       AND rs.start_time = seed.start_time
    CROSS JOIN swagger_member
)
INSERT INTO reservations (
    reservation_no,
    health_center_id,
    member_id,
    service_type_id,
    reservation_slot_id,
    visitor_name,
    visitor_phone,
    status,
    reserved_at,
    created_at
)
SELECT
    reservation_no,
    health_center_id,
    member_id,
    service_type_id,
    reservation_slot_id,
    visitor_name,
    visitor_phone,
    'RESERVED',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM swagger_target_reservation
ON CONFLICT (reservation_no) DO UPDATE
SET health_center_id = EXCLUDED.health_center_id,
    member_id = EXCLUDED.member_id,
    service_type_id = EXCLUDED.service_type_id,
    reservation_slot_id = EXCLUDED.reservation_slot_id,
    visitor_name = EXCLUDED.visitor_name,
    visitor_phone = EXCLUDED.visitor_phone,
    status = 'RESERVED',
    canceled_at = NULL,
    checked_in_at = NULL,
    updated_at = CURRENT_TIMESTAMP;

DELETE FROM queue_tickets
WHERE visit_id IN (
    SELECT v.id
    FROM visits v
    INNER JOIN reservations r ON r.id = v.reservation_id
    WHERE r.reservation_no IN ('RSV-SWAGGER-CHECKIN-001', 'RSV-SWAGGER-CANCEL-001', 'RSV-SWAGGER-DETAIL-001')
);

DELETE FROM visits
WHERE reservation_id IN (
    SELECT id
    FROM reservations
    WHERE reservation_no IN ('RSV-SWAGGER-CHECKIN-001', 'RSV-SWAGGER-CANCEL-001', 'RSV-SWAGGER-DETAIL-001')
);

UPDATE reservation_slots rs
SET reserved_count = LEAST(rs.capacity, GREATEST(rs.reserved_count, swagger_slot_count.seed_count)),
    updated_at = CURRENT_TIMESTAMP
FROM (
    SELECT reservation_slot_id, COUNT(*)::integer AS seed_count
    FROM reservations
    WHERE reservation_no IN ('RSV-SWAGGER-CHECKIN-001', 'RSV-SWAGGER-CANCEL-001', 'RSV-SWAGGER-DETAIL-001')
      AND status = 'RESERVED'
    GROUP BY reservation_slot_id
) swagger_slot_count
WHERE swagger_slot_count.reservation_slot_id = rs.id;

DELETE FROM queue_tickets
WHERE visit_id IN (
    SELECT id
    FROM visits
    WHERE visitor_name IN ('Swagger대기열', 'Swagger대기취소')
      AND visitor_phone IN ('010-5678-9012', '010-6789-0123')
      AND visit_type = 'WALK_IN'
);

DELETE FROM visits
WHERE visitor_name IN ('Swagger대기열', 'Swagger대기취소')
  AND visitor_phone IN ('010-5678-9012', '010-6789-0123')
  AND visit_type = 'WALK_IN';

WITH swagger_queue_service_type AS (
    SELECT id AS service_type_id,
           health_center_id
    FROM service_types
    WHERE health_center_id = 1
      AND code = 'VACCINATION'
      AND active = true
    LIMIT 1
),
swagger_queue_targets AS (
    SELECT 'Swagger대기열' AS visitor_name,
           '010-5678-9012' AS visitor_phone
    UNION ALL
    SELECT 'Swagger대기취소',
           '010-6789-0123'
),
swagger_queue_visit AS (
    INSERT INTO visits (
        health_center_id,
        service_type_id,
        registered_by,
        visitor_name,
        visitor_phone,
        visit_type,
        status,
        checked_in_at,
        created_at
    )
    SELECT
        service_type.health_center_id,
        service_type.service_type_id,
        staff.id,
        target.visitor_name,
        target.visitor_phone,
        'WALK_IN',
        'WAITING',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    FROM swagger_queue_service_type service_type
    INNER JOIN members staff ON staff.email = 'staff@test.com'
    CROSS JOIN swagger_queue_targets target
    RETURNING id, health_center_id, service_type_id, visitor_name
)
INSERT INTO queue_tickets (
    health_center_id,
    visit_id,
    service_type_id,
    ticket_number,
    status,
    issued_at,
    created_at
)
SELECT
    visit.health_center_id,
    visit.id,
    visit.service_type_id,
    (
        SELECT COALESCE(MAX(q.ticket_number), 0) + 1
               + CASE WHEN visit.visitor_name = 'Swagger대기취소' THEN 1 ELSE 0 END
        FROM queue_tickets q
        WHERE q.health_center_id = visit.health_center_id
          AND q.service_type_id = visit.service_type_id
          AND q.issued_at::date = CURRENT_DATE
    ),
    'WAITING',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM swagger_queue_visit visit;

INSERT INTO queue_ticket_counters (
    health_center_id,
    service_type_id,
    issued_date,
    last_ticket_number,
    created_at,
    updated_at
)
SELECT
    q.health_center_id,
    q.service_type_id,
    q.issued_at::date,
    MAX(q.ticket_number),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM queue_tickets q
WHERE q.issued_at::date = CURRENT_DATE
GROUP BY q.health_center_id, q.service_type_id, q.issued_at::date
ON CONFLICT (health_center_id, service_type_id, issued_date) DO UPDATE
SET last_ticket_number = GREATEST(queue_ticket_counters.last_ticket_number, EXCLUDED.last_ticket_number),
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO service_windows (health_center_id, window_number, name, status, active)
VALUES
    (1, 1, '1번 창구', 'OPEN', true),
    (1, 2, '2번 창구', 'OPEN', true),
    (1, 3, '상담 창구', 'OPEN', true)
ON CONFLICT (health_center_id, window_number) DO UPDATE
SET name = EXCLUDED.name,
    status = EXCLUDED.status,
    active = EXCLUDED.active,
    updated_at = CURRENT_TIMESTAMP;

INSERT INTO service_window_service_types (service_window_id, service_type_id, active)
SELECT sw.id,
       st.id,
       true
FROM service_windows sw
INNER JOIN service_types st
    ON st.health_center_id = sw.health_center_id
WHERE sw.health_center_id = 1
  AND (
      (sw.window_number = 1 AND st.code IN ('VACCINATION'))
      OR (sw.window_number = 2 AND st.code IN ('HEALTH_CHECK'))
      OR (sw.window_number = 3 AND st.code IN ('HEALTH_CONSULT'))
  )
ON CONFLICT (service_window_id, service_type_id) DO UPDATE
SET active = EXCLUDED.active,
    updated_at = CURRENT_TIMESTAMP;
