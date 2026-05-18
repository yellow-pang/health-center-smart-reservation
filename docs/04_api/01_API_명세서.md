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

직접 HTTP 호출이나 curl에서는 `Bearer ` 접두사를 포함한다.
Swagger Authorize 창은 HTTP bearer 스키마를 사용하므로 `Bearer `를 제외한 accessToken 값만 입력한다.

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
| Queue API | 대기번호, 대기열, 호출, 보류, 최종 미응답, 취소, 처리 |
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
| GET | /api/auth/social/{provider}/authorize | 소셜 로그인 시작 | PUBLIC |
| GET | /api/auth/social/{provider}/callback | 소셜 로그인 콜백 | PUBLIC |
| POST | /api/auth/social/signup | 소셜 로그인 추가 정보 입력 완료 | PUBLIC |
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
| POST | /api/queues/{id}/no-show | 최종 미응답 처리 | STAFF, ADMIN |
| POST | /api/queues/{id}/cancel | 방문/대기 취소 | STAFF, ADMIN |
| GET | /api/dashboard/summary | 대시보드 요약 | ADMIN |
| GET | /api/dashboard/hourly-visits | 시간대별 방문자 수 | ADMIN |
| GET | /api/dashboard/service-wait-times | 업무별 평균 대기시간 | ADMIN |
| GET | /api/dashboard/visit-type-ratio | 예약/현장 비율 | ADMIN |
| GET | /api/dashboard/no-show-rate | 노쇼율 | ADMIN |
| GET | /api/congestion/current | 현재 혼잡도 | PUBLIC |
| GET | /api/admin/service-types | 관리자 업무 유형 전체 조회 | ADMIN |
| POST | /api/admin/service-types | 업무 유형 생성 | ADMIN |
| PUT | /api/admin/service-types/{id} | 업무 유형 수정 | ADMIN |
| PATCH | /api/admin/service-types/{id}/deactivate | 업무 유형 비활성화 | ADMIN |
| PATCH | /api/admin/service-types/{id}/activate | 업무 유형 재활성화 | ADMIN |
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

### 4.1.1 소셜 로그인

`GET /api/auth/social/{provider}/authorize`

Path:

| 이름 | 설명 | 예시 |
|---|---|---|
| provider | 소셜 로그인 제공자 | `kakao`, `naver`, `google` |

동작:

- 백엔드가 제공자별 OAuth 인증 URL로 302 redirect한다.
- 실제 사용 전 OAuth 환경변수와 제공자 개발자 콘솔 Redirect URI가 일치해야 한다.

Callback:

`GET /api/auth/social/{provider}/callback?code={authorizationCode}`

동작:

- 백엔드가 provider access token과 프로필을 조회한다.
- `social_accounts` 매핑 또는 이메일 기준으로 회원을 찾고, 없으면 `CITIZEN` 회원을 생성한다.
- 프로젝트 JWT access token과 refresh token을 발급한다.
- 프론트 `/login/social/callback#accessToken=...&refreshToken=...&role=...`로 302 redirect한다.
- provider가 이메일을 제공하지 않아 신규 회원을 바로 만들 수 없으면 프론트 `/login/social/callback#profileRequired=true&completionToken=...`로 302 redirect한다.

추가 정보 입력 완료:

`POST /api/auth/social/signup`

Request:

```json
{
  "completionToken": "social-signup-token",
  "email": "citizen@example.com",
  "name": "홍길동"
}
```

동작:

- `completionToken`을 검증해 provider와 provider 사용자 식별자를 확인한다.
- 입력받은 이메일과 이름으로 `CITIZEN` 회원을 생성하고 `social_accounts`에 연결한다.
- 이미 가입된 이메일이면 기존 계정 임의 연결을 막기 위해 실패 처리한다.
- 성공 시 일반 로그인과 동일하게 access token과 refresh token을 반환한다.

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

### 4.5.2 관리자 업무 유형 전체 조회

`GET /api/admin/service-types`

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
    },
    {
      "id": 4,
      "healthCenterId": 1,
      "code": "MATERNAL_HEALTH",
      "name": "모자보건",
      "description": "모자보건 상담 및 접수",
      "defaultCapacity": 5,
      "active": false
    }
  ],
  "error": null
}
```

### 4.5.3 관리자 업무 유형 생성

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

### 4.5.4 관리자 업무 유형 재활성화

`PATCH /api/admin/service-types/{id}/activate`

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

### 4.5.5 관리자 직원 목록 조회

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

권한:

- 같은 보건소 소속 `STAFF`, `ADMIN`

정책:

- 예약 없이 방문자를 접수한다.
- 활성 업무 유형만 접수할 수 있다.
- 현장 접수 성공 시 `visits`에 현장 방문 이력이 생성되고, `queue_tickets`에 `WAITING` 대기번호가 발급된다.
- Swagger 대표 예시는 기존 업무 유형 seed 중 `serviceTypeId = 1`을 사용한다. 실제 ID가 다르면 `GET /api/service-types`에서 확인한 ID로 바꿔 실행한다.
- Swagger Authorize 창에는 accessToken 값만 입력한다. curl 같은 직접 호출에서는 `Authorization: Bearer {accessToken}` 형식을 사용한다.

Request:

```json
{
  "serviceTypeId": 1,
  "visitorName": "Swagger현장접수",
  "visitorPhone": "010-4567-8901"
}
```

Response:

```json
{
  "success": true,
  "data": {
    "visitId": 201,
    "queueTicketId": 301,
    "ticketNumber": 16,
    "status": "WAITING"
  },
  "error": null
}
```

### 4.9 대기열 조회와 상태 전이

#### 4.9.1 대기열 조회

`GET /api/queues?serviceTypeId=1&status=WAITING`

권한:

- 같은 보건소 소속 `STAFF`, `ADMIN`

정책:

- 오늘 발급된 대기표를 조회한다.
- `serviceTypeId`를 생략하면 전체 업무 유형을 조회한다.
- `status`를 생략하면 `WAITING`, `CALLED`, `IN_PROGRESS`, `HOLD` 상태를 조회한다.
- 대기번호는 같은 보건소, 같은 업무 유형, 같은 발급일 기준으로 중복되지 않도록 DB 채번 테이블과 유니크 인덱스로 보호한다.
- Swagger 테스트용 seed 방문자 이름은 `Swagger대기열`이며, 조회 결과에서 `queueTicketId`를 확인해 호출/시작/완료에 사용한다.

Response:

```json
{
  "success": true,
  "data": [
    {
      "queueTicketId": 301,
      "visitId": 201,
      "serviceTypeId": 1,
      "serviceTypeName": "예방접종",
      "ticketNumber": 16,
      "status": "WAITING",
      "visitType": "WALK_IN",
      "visitorName": "Swagger대기열",
      "visitorPhone": "010-5678-9012",
      "issuedAt": "2026-05-11T10:30:00",
      "calledAt": null,
      "startedAt": null,
      "completedAt": null,
      "holdAt": null
    }
  ],
  "error": null
}
```

#### 4.9.2 대기자 호출

`POST /api/queues/{queueTicketId}/call`

정책:

- `WAITING`, `HOLD` 상태만 호출할 수 있다.
- 성공 시 상태는 `CALLED`가 되고 `calledAt`이 기록된다.

#### 4.9.3 처리 시작

`POST /api/queues/{queueTicketId}/start`

정책:

- `CALLED` 상태만 처리 시작할 수 있다.
- 성공 시 대기표 상태는 `IN_PROGRESS`가 되고 Visit 상태도 `IN_PROGRESS`가 된다.

#### 4.9.4 보류 처리

`POST /api/queues/{queueTicketId}/hold`

정책:

- `CALLED` 상태만 보류 처리할 수 있다.
- 성공 시 대기표 상태는 `HOLD`가 되고 `holdAt`이 기록된다.
- `HOLD` 상태 대기표는 `POST /api/queues/{queueTicketId}/call`로 재호출할 수 있다.

#### 4.9.5 최종 미응답 처리

`POST /api/queues/{queueTicketId}/no-show`

정책:

- `HOLD` 상태만 최종 미응답 처리할 수 있다.
- 성공 시 대기표 상태는 `NO_SHOW`가 되고 Visit 상태도 `NO_SHOW`가 된다.
- 예약 기반 방문이면 `CHECKED_IN` 상태 예약도 `NO_SHOW`로 변경된다.

#### 4.9.6 방문/대기 취소

`POST /api/queues/{queueTicketId}/cancel`

정책:

- `WAITING`, `CALLED`, `HOLD` 상태만 취소할 수 있다.
- 성공 시 대기표 상태는 `CANCELED`가 되고 Visit 상태도 `CANCELED`가 된다.
- 예약 기반 방문이면 `CHECKED_IN` 상태 예약도 `CANCELED`로 변경한다.
- 처리 시작 이후(`IN_PROGRESS`, `COMPLETED`)와 이미 종료된 `NO_SHOW`, `CANCELED` 상태는 취소할 수 없다.
- Swagger 테스트용 seed 방문자 이름은 `Swagger대기취소`이며, 조회 결과에서 `queueTicketId`를 확인해 취소에 사용한다.

#### 4.9.7 처리 완료

`POST /api/queues/{queueTicketId}/complete`

정책:

- `IN_PROGRESS` 상태만 완료할 수 있다.
- 성공 시 대기표 상태는 `COMPLETED`가 되고 Visit 상태도 `COMPLETED`가 된다.
- 예약 기반 방문이면 예약 상태도 `COMPLETED`로 변경된다.

상태 오류 Response:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "QUEUE_INVALID_STATUS",
    "message": "호출할 수 없는 대기 상태입니다."
  }
}
```

### 4.10 대시보드 요약

`GET /api/dashboard/summary?date=2026-05-10`

권한:

- `ADMIN`

정책:

- `date`를 생략하면 오늘 날짜 기준으로 조회한다.
- 로그인한 관리자의 `healthCenterId` 기준으로 집계한다.
- 오늘 방문자 수는 `visits.checked_in_at` 기준이다.
- 현재 대기 인원은 해당 날짜에 발급된 `WAITING` 대기표 수 기준이다.
- 평균 대기시간은 `called_at - issued_at` 평균값이다.
- 노쇼율은 취소 예약을 제외한 예약 중 `NO_SHOW` 비율이다.

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

### 4.11 대시보드 세부 지표

아래 API는 모두 `ADMIN` 권한이 필요하며, `date`를 생략하면 오늘 날짜 기준으로 조회한다.

#### 4.11.1 시간대별 방문자 수

`GET /api/dashboard/hourly-visits?date=2026-05-10`

정책:

- 로그인한 관리자의 `healthCenterId` 기준으로 집계한다.
- 0시부터 23시까지 모든 시간대를 반환한다.
- 방문자 수는 `visits.checked_in_at` 기준이다.

Response:

```json
{
  "success": true,
  "data": [
    {
      "hour": 9,
      "visitCount": 12
    }
  ],
  "error": null
}
```

#### 4.11.2 업무별 평균 대기시간

`GET /api/dashboard/service-wait-times?date=2026-05-10`

정책:

- 활성 업무 유형별 평균 대기시간을 조회한다.
- 평균 대기시간은 `called_at - issued_at` 기준이다.
- 호출 이력이 없는 업무 유형은 평균 대기시간 `0`, 호출 건수 `0`으로 반환한다.

Response:

```json
{
  "success": true,
  "data": [
    {
      "serviceTypeId": 1,
      "serviceTypeName": "예방접종",
      "averageWaitMinutes": 24,
      "calledCount": 18
    }
  ],
  "error": null
}
```

#### 4.11.3 예약/현장 방문 비율

`GET /api/dashboard/visit-type-ratio?date=2026-05-10`

정책:

- 방문 유형 `RESERVED`, `WALK_IN` 기준으로 비율을 계산한다.
- 전체 방문 수가 0이면 각 비율은 `0.0`으로 반환한다.

Response:

```json
{
  "success": true,
  "data": {
    "totalVisitCount": 120,
    "reservedVisitCount": 80,
    "walkInVisitCount": 40,
    "reservedVisitRatio": 66.7,
    "walkInVisitRatio": 33.3
  },
  "error": null
}
```

#### 4.11.4 노쇼율

`GET /api/dashboard/no-show-rate?date=2026-05-10`

정책:

- 취소 예약은 계산 대상에서 제외한다.
- 계산 대상 예약 수가 0이면 노쇼율은 `0.0`으로 반환한다.

Response:

```json
{
  "success": true,
  "data": {
    "targetReservationCount": 100,
    "noShowReservationCount": 8,
    "noShowRate": 8.0
  },
  "error": null
}
```

### 4.12 현재 혼잡도

`GET /api/congestion/current?healthCenterId=1`

정책:

- `healthCenterId`를 생략하면 기본 보건소 `1` 기준으로 조회한다.
- 활성 업무 유형별 현재 `WAITING` 대기 인원과 예상 대기시간을 조회한다.
- 예상 대기시간은 `현재 대기 인원 * 평균 처리시간`이다.
- 평균 처리시간은 오늘 완료된 대기표의 `completed_at - started_at` 평균값이며, 완료 데이터가 없으면 5분을 사용한다.
- 혼잡도는 대기 인원 기준과 예상 대기시간 기준 중 더 높은 수준을 사용한다.

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
