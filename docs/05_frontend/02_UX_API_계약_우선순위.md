# UX/API 계약 우선순위

## 문서 목적

이 문서는 프론트엔드 화면 흐름과 백엔드 API 계약을 함께 설계하기 위한 기준 문서이다.

이미 `docs/05_frontend/01_화면_설계서_v0_프롬프트.md`에는 화면 구성과 v0 프롬프트가 있고, `docs/04_api/01_API_명세서.md`에는 전체 API 목록이 있다. 이 문서는 두 문서를 반복하지 않고, 어떤 화면에서 어떤 API 응답이 필요한지 연결하는 데 집중한다.

## 중복 제외 기준

이 문서에서 다시 작성하지 않는 내용:

- 전체 화면 프롬프트: `docs/05_frontend`
- 전체 API 목록과 상세 예시: `docs/04_api`
- 업무 흐름도: `docs/02_domain/02_업무_흐름도.md`

이 문서에서 추가로 정리하는 내용:

- 화면 우선순위별 API 계약
- mock data로 먼저 확인할 흐름
- API 응답에서 UX를 위해 필요한 필드
- 백엔드에서 반드시 검증해야 하는 정책

## 개발 방식

이 프로젝트는 아래 방식으로 진행한다.

```text
화면 흐름 초안
→ 화면별 필요한 데이터 정의
→ API 요청/응답 계약 정의
→ 프론트 mock data 구현
→ 백엔드 API 구현
→ 실제 API 연동
```

원칙:

- UX를 먼저 보되, 핵심 정책은 백엔드에서 검증한다.
- 프론트는 상태명과 표시명을 하드코딩하지 않는다.
- 화면에서 쓰지 않는 과한 데이터를 응답하지 않는다.
- 직원 화면은 클릭 수와 업무 처리 시간을 줄이는 방향으로 설계한다.

## 화면/API 우선순위

| 순서 | 화면 | 필요한 API | 우선 이유 |
|---:|---|---|---|
| 1 | 업무 유형 선택 | `GET /api/service-types` | 예약 흐름의 시작 |
| 2 | 예약 가능 시간 조회 | `GET /api/reservation-slots` | 사용자 예약 UX 핵심 |
| 3 | 예약 신청 | `POST /api/reservations` | 사용자 핵심 기능 |
| 4 | 내 예약 조회/취소 | `GET /api/reservations/me`, `DELETE /api/reservations/{id}` | 예약 후 관리 |
| 5 | 로그인 | `POST /api/auth/login` | 권한별 화면 진입 |
| 6 | 직원 체크인 | `POST /api/visits/check-in` | 현장 운영 시작 |
| 7 | 현장 접수 | `POST /api/visits/walk-in` | 예약 없는 방문자 처리 |
| 8 | 대기열 관리 | `GET /api/queues`, queue command APIs | 직원 업무 효율 핵심 |
| 9 | 관리자 대시보드 | dashboard APIs | 포트폴리오 시각화 핵심 |
| 10 | 현재 혼잡도 | `GET /api/congestion/current` | 사용자 방문 판단 지원 |

주의:

- 인증 API가 기술적으로 먼저 필요할 수 있지만, UX/API 계약 설계는 업무 유형과 예약 화면부터 잡는 것이 이해하기 쉽다.
- 실제 구현에서는 인증/권한을 예약 생성 전까지 반드시 연결해야 한다.

## 사용자 예약 화면 계약

### 업무 유형 선택

API:

```text
GET /api/service-types
```

화면에 필요한 필드:

| 필드 | 용도 |
|---|---|
| `serviceTypeId` | 다음 예약 슬롯 조회 요청 |
| `code` | 업무 유형 식별 |
| `name` | 화면 표시 |
| `description` | 안내 문구 |
| `defaultCapacity` | 관리자 또는 디버그 표시 |
| `active` | 비활성 업무 제외 |

UX 고도화:

- 업무별 예상 소요 시간 또는 안내 문구를 표시할 수 있다.
- 업무 유형이 마감 또는 임시 중지 상태면 선택 불가 처리할 수 있다.

### 예약 가능 시간 조회

API:

```text
GET /api/reservation-slots?serviceTypeId=1&date=2026-05-10
```

화면에 필요한 필드:

| 필드 | 용도 |
|---|---|
| `slotId` | 예약 신청 요청 |
| `date` | 선택 날짜 표시 |
| `startTime` | 시간 버튼 표시 |
| `endTime` | 시간 범위 표시 |
| `capacity` | 정원 표시 |
| `reservedCount` | 현재 예약 수 |
| `availableCount` | 잔여 자리 표시 |
| `available` | 버튼 활성 여부 |

UX 고도화:

- 마감 슬롯은 회색 비활성으로 표시한다.
- 잔여 1~2자리는 “마감 임박”으로 표시한다.
- 날짜별 예약 가능 여부를 캘린더에 요약 표시할 수 있다.

백엔드 필수 검증:

- 사용자가 비활성 슬롯 ID를 직접 보내도 예약되지 않아야 한다.
- 정원 초과와 중복 예약은 서버에서 검증해야 한다.

## 직원 운영 화면 계약

### 예약자 체크인

API:

```text
POST /api/visits/check-in
```

요청:

```json
{
  "reservationNo": "RSV-20260510-0001"
}
```

응답에 필요한 필드:

| 필드 | 용도 |
|---|---|
| `visitId` | 방문 이력 식별 |
| `queueTicketId` | 대기표 식별 |
| `ticketNumber` | 현장 안내 |
| `serviceTypeName` | 업무 구분 |
| `status` | 대기 상태 |
| `estimatedWaitMinutes` | 안내 문구 |

UX 고도화:

- 체크인 성공 시 대기번호를 크게 표시한다.
- 이미 체크인된 예약은 명확한 오류 메시지를 보여준다.
- 예약 시간 10분 초과 여부를 직원에게 안내한다.

### 대기열 관리

API:

```text
GET /api/queues?serviceTypeId=1
POST /api/queues/{id}/call
POST /api/queues/{id}/start
POST /api/queues/{id}/complete
POST /api/queues/{id}/hold
```

화면에 필요한 필드:

| 필드 | 용도 |
|---|---|
| `queueTicketId` | 상태 변경 요청 |
| `ticketNumber` | 대기번호 표시 |
| `visitorNameMasked` | 개인정보 보호 |
| `serviceTypeName` | 업무 필터 |
| `visitType` | 예약/현장 구분 |
| `status` | 버튼 노출 조건 |
| `waitingMinutes` | 대기시간 표시 |
| `calledAt` | 호출 경과 시간 계산 |

UX 고도화:

- 상태별 가능한 버튼만 보여준다.
- 호출 중인 대기번호를 화면 상단에 크게 표시한다.
- 오래 기다린 대기자는 강조 표시한다.
- HOLD 상태는 재호출 버튼을 별도로 표시한다.

백엔드 필수 검증:

- COMPLETED 상태는 다시 호출할 수 없어야 한다.
- 권한 없는 사용자는 직원 API를 호출할 수 없어야 한다.
- 상태 전이는 Queue Policy에서 검증한다.

## 관리자 대시보드 계약

API:

```text
GET /api/dashboard/summary
GET /api/dashboard/hourly-visits
GET /api/dashboard/service-wait-times
GET /api/dashboard/visit-type-ratio
GET /api/dashboard/no-show-rate
```

차트 친화 응답 기준:

| 데이터 | 권장 응답 형태 |
|---|---|
| KPI | 숫자 값과 전일 대비 변화량 |
| 시간대별 방문자 | `timeBucket`, `visitCount` |
| 업무별 대기시간 | `serviceTypeName`, `averageWaitMinutes` |
| 예약/현장 비율 | `visitType`, `count`, `ratio` |
| 노쇼율 | `date`, `reservationCount`, `noShowCount`, `rate` |

UX 고도화:

- 관리자는 오늘 기준 요약을 먼저 본다.
- 기간 필터를 제공한다.
- 혼잡 시간대를 자동으로 강조한다.
- 운영 개선 제안 문구를 데이터 기반으로 표시한다.

## Mock data 우선 작성 대상

API 구현 전 프론트에서 먼저 확인할 mock data:

| 우선순위 | Mock | 목적 |
|---:|---|---|
| 1 | 업무 유형 3개 | 예약 시작 흐름 |
| 2 | 날짜별 예약 슬롯 | 시간 선택 UX |
| 3 | 내 예약 목록 | 취소 가능/불가 표시 |
| 4 | 대기열 목록 | 직원 업무 화면 |
| 5 | 대시보드 KPI/차트 | 관리자 화면 |
| 6 | 공통코드 목록 | 상태 배지와 표시명 |

## 완료 기준

이 문서는 아래 조건을 만족하면 실제 구현 기준으로 사용할 수 있다.

- 화면별 필요한 API가 연결되어 있다.
- API 응답 필드가 화면 사용성과 연결되어 있다.
- 백엔드에서 반드시 검증할 정책이 구분되어 있다.
- mock data로 프론트 화면을 먼저 만들 수 있다.
- 백엔드는 이 계약을 기준으로 Controller/Service/Mapper를 구현할 수 있다.
