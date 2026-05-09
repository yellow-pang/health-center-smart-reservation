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
