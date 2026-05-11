# API 명세서

## 1. API 공통 규칙

### Base URL

```text
/api
```

### 인증 방식

```text
Authorization: Bearer {accessToken}
```

### 공통 응답 형식

```json
{
  "success": true,
  "data": {},
  "error": null
}
```

### 공통 오류 응답 형식

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "RESERVATION_SLOT_FULL",
    "message": "선택한 시간대의 예약이 마감되었습니다."
  }
}
```

## 2. API 그룹

| 그룹 | 설명 |
|---|---|
| Auth API | 로그인, 토큰 재발급, 로그아웃 |
| Member API | 회원 정보 |
| Office API | 보건소, 업무 유형, 창구 |
| Reservation API | 예약 가능 시간, 예약 신청, 예약 조회, 예약 취소 |
| Visit API | 체크인, 현장 접수 |
| Queue API | 대기번호, 대기열, 호출, 처리 |
| Dashboard API | 관리자 대시보드 |
| Congestion API | 사용자용 혼잡도 |
| Admin API | 관리자 기준정보 관리 |
| Common API | 공통코드 |

## 3. 주요 API 목록

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| POST | /api/auth/login | 로그인 | PUBLIC |
| POST | /api/auth/reissue | 토큰 재발급 | PUBLIC |
| POST | /api/auth/logout | 로그아웃 | 로그인 사용자 |
| GET | /api/members/me | 내 회원 정보 조회 | 로그인 사용자 |
| GET | /api/service-types | 업무 유형 조회 | PUBLIC |
| GET | /api/reservation-slots | 예약 가능 시간 조회 | 로그인 사용자 |
| POST | /api/reservations | 예약 신청 | CITIZEN, GUARDIAN, STAFF, ADMIN |
| GET | /api/reservations/me | 내 예약 조회 | 로그인 사용자 |
| GET | /api/reservations/{id} | 예약 상세 조회 | 예약자, STAFF, ADMIN |
| DELETE | /api/reservations/{id} | 예약 취소 | 예약자, ADMIN |
| POST | /api/visits/check-in | 예약자 체크인 | STAFF, ADMIN |
| POST | /api/visits/walk-in | 현장 접수 | STAFF, ADMIN |
| GET | /api/queues | 대기열 조회 | STAFF, ADMIN |
| POST | /api/queues/{id}/call | 대기자 호출 | STAFF, ADMIN |
| POST | /api/queues/{id}/start | 처리 시작 | STAFF, ADMIN |
| POST | /api/queues/{id}/complete | 처리 완료 | STAFF, ADMIN |
| POST | /api/queues/{id}/hold | 보류 처리 | STAFF, ADMIN |
| GET | /api/dashboard/summary | 대시보드 요약 | ADMIN |
| GET | /api/dashboard/hourly-visits | 시간대별 방문자 수 | ADMIN |
| GET | /api/dashboard/service-wait-times | 업무별 평균 대기시간 | ADMIN |
| GET | /api/dashboard/visit-type-ratio | 예약/현장 비율 | ADMIN |
| GET | /api/dashboard/no-show-rate | 노쇼율 | ADMIN |
| GET | /api/congestion/current | 현재 혼잡도 | PUBLIC |
| POST | /api/admin/service-types | 업무 유형 생성 | ADMIN |
| PUT | /api/admin/service-types/{id} | 업무 유형 수정 | ADMIN |
| PATCH | /api/admin/service-types/{id}/deactivate | 업무 유형 비활성화 | ADMIN |
| POST | /api/admin/reservation-slots | 예약 슬롯 생성 | ADMIN |
| GET | /api/admin/staff | 직원 목록 조회 | ADMIN |
| GET | /api/admin/service-windows | 창구 업무 매핑 조회 | ADMIN |

## 4. 상세 예시

### 4.1 로그인

`POST /api/auth/login`

Request:

```json
{
  "email": "staff@example.com",
  "password": "password1234"
}
```

Response:

```json
{
  "success": true,
  "data": {
    "accessToken": "access-token",
    "refreshToken": "refresh-token",
    "member": {
      "id": 1,
      "healthCenterId": 1,
      "email": "staff@test.com",
      "name": "보건소 직원",
      "role": "STAFF"
    }
  },
  "error": null
}
```

### 4.2 토큰 재발급

`POST /api/auth/reissue`

Request:

```json
{
  "refreshToken": "refresh-token"
}
```

Response:

```json
{
  "success": true,
  "data": {
    "accessToken": "new-access-token",
    "refreshToken": "new-refresh-token",
    "member": {
      "id": 1,
      "healthCenterId": 1,
      "email": "staff@test.com",
      "name": "보건소 직원",
      "role": "STAFF"
    }
  },
  "error": null
}
```

### 4.3 로그아웃

`POST /api/auth/logout`

Request:

```json
{
  "refreshToken": "refresh-token"
}
```

Response:

```json
{
  "success": true,
  "data": null,
  "error": null
}
```

### 4.4 내 회원 정보 조회

`GET /api/members/me`

Response:

```json
{
  "success": true,
  "data": {
    "id": 2,
    "healthCenterId": 1,
    "email": "staff@test.com",
    "name": "보건소 직원",
    "role": "STAFF"
  },
  "error": null
}
```

### 4.5 예약 가능 시간 조회

`GET /api/reservation-slots?serviceTypeId=1&date=2026-05-10`

Response:

```json
{
  "success": true,
  "data": [
    {
      "slotId": 10,
      "date": "2026-05-10",
      "startTime": "09:00",
      "endTime": "09:30",
      "capacity": 5,
      "reservedCount": 2,
      "availableCount": 3,
      "available": true
    }
  ],
  "error": null
}
```

### 4.5.0 관리자 예약 슬롯 생성

`POST /api/admin/reservation-slots`

Request:

```json
{
  "serviceTypeId": 1,
  "date": "2026-05-10",
  "startTime": "09:00",
  "endTime": "09:30",
  "capacity": 5
}
```

Response:

```json
{
  "success": true,
  "data": {
    "slotId": 10,
    "serviceTypeId": 1,
    "serviceTypeName": "예방접종",
    "date": "2026-05-10",
    "startTime": "09:00",
    "endTime": "09:30",
    "capacity": 5,
    "reservedCount": 0,
    "availableCount": 5,
    "available": true
  },
  "error": null
}
```

### 4.5.1 업무 유형 조회

`GET /api/service-types`

Response:

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "healthCenterId": 1,
      "code": "VACCINATION",
      "name": "예방접종",
      "description": "예방접종 예약 및 현장 접수",
      "defaultCapacity": 5,
      "active": true
    }
  ],
  "error": null
}
```

### 4.5.2 관리자 업무 유형 생성

`POST /api/admin/service-types`

Request:

```json
{
  "code": "MATERNAL_HEALTH",
  "name": "모자보건",
  "description": "모자보건 상담 및 접수",
  "defaultCapacity": 5
}
```

Response:

```json
{
  "success": true,
  "data": {
    "id": 4,
    "healthCenterId": 1,
    "code": "MATERNAL_HEALTH",
    "name": "모자보건",
    "description": "모자보건 상담 및 접수",
    "defaultCapacity": 5,
    "active": true
  },
  "error": null
}
```

### 4.5.3 관리자 직원 목록 조회

`GET /api/admin/staff`

Response:

```json
{
  "success": true,
  "data": [
    {
      "id": 2,
      "healthCenterId": 1,
      "email": "staff@test.com",
      "name": "보건소 직원",
      "phone": "010-0000-0002",
      "role": "STAFF",
      "active": true
    }
  ],
  "error": null
}
```

### 4.5.4 관리자 창구 업무 매핑 조회

`GET /api/admin/service-windows`

Response:

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "healthCenterId": 1,
      "windowNumber": 1,
      "name": "1번 창구",
      "status": "OPEN",
      "active": true,
      "serviceTypes": [
        {
          "id": 1,
          "healthCenterId": 1,
          "code": "VACCINATION",
          "name": "예방접종",
          "description": "예방접종 예약 및 현장 접수",
          "defaultCapacity": 5,
          "active": true
        }
      ]
    }
  ],
  "error": null
}
```

### 4.6 예약 신청

`POST /api/reservations`

Request:

```json
{
  "serviceTypeId": 1,
  "reservationSlotId": 10,
  "visitorName": "홍길동",
  "visitorPhone": "010-1234-5678"
}
```

Response:

```json
{
  "success": true,
  "data": {
    "reservationId": 100,
    "reservationNo": "RSV-20260510-0001",
    "status": "RESERVED"
  },
  "error": null
}
```

### 4.6.1 내 예약 조회

`GET /api/reservations/me`

Response:

```json
{
  "success": true,
  "data": [
    {
      "reservationId": 100,
      "reservationNo": "RSV-20260510-0001",
      "serviceTypeId": 1,
      "serviceTypeName": "예방접종",
      "reservationSlotId": 10,
      "date": "2026-05-10",
      "startTime": "09:00",
      "endTime": "09:30",
      "visitorName": "홍길동",
      "visitorPhone": "010-1234-5678",
      "status": "RESERVED",
      "reservedAt": "2026-05-10T08:30:00"
    }
  ],
  "error": null
}
```

### 4.6.2 예약 상세 조회

`GET /api/reservations/{id}`

Swagger 테스트용 seed:

- `RSV-SWAGGER-DETAIL-001`

권한:

- 예약자 본인
- 같은 보건소 소속 `STAFF`, `ADMIN`

Response:

```json
{
  "success": true,
  "data": {
    "reservationId": 100,
    "reservationNo": "RSV-20260510-0001",
    "serviceTypeId": 1,
    "serviceTypeName": "예방접종",
    "reservationSlotId": 10,
    "date": "2026-05-10",
    "startTime": "09:00",
    "endTime": "09:30",
    "visitorName": "홍길동",
    "visitorPhone": "010-1234-5678",
    "status": "RESERVED",
    "reservedAt": "2026-05-10T08:30:00"
  },
  "error": null
}
```

### 4.6.3 예약 취소

`DELETE /api/reservations/{id}`

Swagger 테스트용 seed:

- `RSV-SWAGGER-CANCEL-001`

권한:

- 예약자 본인
- 같은 보건소 소속 `ADMIN`

정책:

- `RESERVED` 상태 예약만 취소할 수 있다.
- 예약 시간 1시간 전까지만 취소할 수 있다.
- 취소 성공 시 예약 상태는 `CANCELED`로 변경되고 예약 슬롯의 `reservedCount`는 1 감소한다.

Response:

```json
{
  "success": true,
  "data": null,
  "error": null
}
```

취소 가능 시간 초과 Response:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "RESERVATION_CANCEL_TIME_EXPIRED",
    "message": "예약 취소는 예약 시간 1시간 전까지만 가능합니다."
  }
}
```

### 4.7 예약자 체크인

`POST /api/visits/check-in`

권한:

- 같은 보건소 소속 `STAFF`, `ADMIN`

정책:

- `RESERVED` 상태 예약만 체크인할 수 있다.
- 체크인 성공 시 예약 상태는 `CHECKED_IN`으로 변경된다.
- 체크인 성공 시 `visits`에 예약 방문 이력이 생성되고, `queue_tickets`에 `WAITING` 대기번호가 발급된다.
- 체크인 이후 기존 예약 취소 API는 `RESERVATION_CANCEL_INVALID_STATUS`로 실패한다.

Request:

```json
{
  "reservationNo": "RSV-SWAGGER-CHECKIN-001"
}
```

Swagger 테스트용 seed:

- `RSV-SWAGGER-CHECKIN-001`

Response:

```json
{
  "success": true,
  "data": {
    "visitId": 200,
    "queueTicketId": 300,
    "ticketNumber": 15,
    "status": "WAITING"
  },
  "error": null
}
```

이미 체크인한 예약 Response:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "ALREADY_CHECKED_IN",
    "message": "이미 체크인했거나 체크인할 수 없는 예약입니다."
  }
}
```

### 4.8 현장 접수

`POST /api/visits/walk-in`

Request:

```json
{
  "serviceTypeId": 1,
  "visitorName": "김철수",
  "visitorPhone": "010-1111-2222"
}
```

### 4.9 대시보드 요약

`GET /api/dashboard/summary?date=2026-05-10`

Response:

```json
{
  "success": true,
  "data": {
    "todayVisitCount": 120,
    "currentWaitingCount": 18,
    "averageWaitMinutes": 24,
    "noShowRate": 8.5
  },
  "error": null
}
```

### 4.10 현재 혼잡도

`GET /api/congestion/current`

Response:

```json
{
  "success": true,
  "data": [
    {
      "serviceTypeId": 1,
      "serviceTypeName": "예방접종",
      "waitingCount": 18,
      "estimatedWaitMinutes": 35,
      "congestionLevel": "HIGH",
      "congestionLabel": "혼잡"
    }
  ],
  "error": null
}
```

## 5. 주요 오류 코드

| 코드 | 설명 |
|---|---|
| AUTH_REQUIRED | 로그인이 필요함 |
| AUTH_INVALID_CREDENTIALS | 이메일 또는 비밀번호가 올바르지 않음 |
| AUTH_TOKEN_EXPIRED | Access Token 만료 |
| AUTH_REFRESH_TOKEN_INVALID | Refresh Token이 유효하지 않음 |
| FORBIDDEN | 접근 권한 없음 |
| SERVICE_TYPE_NOT_FOUND | 업무 유형 없음 |
| RESERVATION_SLOT_NOT_FOUND | 예약 슬롯 없음 |
| RESERVATION_SLOT_FULL | 예약 슬롯 정원 초과 |
| RESERVATION_DUPLICATED | 중복 예약 |
| RESERVATION_NOT_FOUND | 예약 없음 |
| RESERVATION_CANCEL_TIME_EXPIRED | 취소 가능 시간 초과 |
| RESERVATION_CANCEL_INVALID_STATUS | 취소할 수 없는 예약 상태 |
| VISIT_NOT_FOUND | 방문 정보 없음 |
| QUEUE_TICKET_NOT_FOUND | 대기표 없음 |
| QUEUE_INVALID_STATUS | 현재 상태에서 수행할 수 없는 요청 |
