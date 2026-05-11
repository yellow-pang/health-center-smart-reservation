# Waiting Queue Lifecycle API 구현 기록

## 1. 작업 목표

- 직원 또는 관리자가 오늘 대기열을 조회할 수 있게 한다.
- 대기표 상태를 `WAITING -> CALLED -> IN_PROGRESS -> COMPLETED` 순서로 전이할 수 있게 한다.
- 처리 시작/완료 시 Visit 상태를 함께 갱신하고, 예약 기반 방문 완료 시 예약 상태도 `COMPLETED`로 변경한다.

## 2. 작업 범위

- [x] 이번 브랜치에 포함: `GET /api/queues`
- [x] 이번 브랜치에 포함: `POST /api/queues/{id}/call`
- [x] 이번 브랜치에 포함: `POST /api/queues/{id}/start`
- [x] 이번 브랜치에 포함: `POST /api/queues/{id}/complete`
- [x] 이번 브랜치에 포함: Swagger 테스트용 대기표 seed 추가
- [x] 이번 브랜치에서 제외: 보류 `POST /api/queues/{id}/hold`
- [x] 이번 브랜치에서 제외: 대기번호 발급 동시성 정책 보강
- [x] 이번 브랜치에서 제외: 방문/대기 취소 전용 API

## 3. 작업 전 체크리스트

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 기준 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 관련 항목 확인
- [x] `docs/02_domain/02_업무_흐름도.md` 확인
- [x] `docs/04_api/01_API_명세서.md` 확인
- [x] 현재 브랜치와 작업 트리 확인
- [x] 영향받는 파일 확인

## 4. 영향 분석

GitNexus 확인:

| 대상 | 결과 | 보완 확인 |
|---|---|---|
| `gitnexus status` | stale index 확인 | `rg`와 Maven 검증 병행 |
| `gitnexus impact QueueTicketMapper` | LOW, 직접 영향 `VisitCommandService` | Queue API 추가 영향은 신규 Controller/Service 중심 |
| `gitnexus impact QueueTicketVO` | LOW, 직접 영향 `VisitCommandService` | VO 필드 확장 후 기존 체크인/현장접수 응답 영향 없음 |
| `gitnexus impact SecurityConfig` | LOW, 직접 영향 없음 | 기존 `/api/queues/**` STAFF/ADMIN 권한 규칙 확인 |
| `gitnexus impact VisitMapper` | LOW, 직접 영향 `VisitCommandService` | 이번 작업에서는 직접 수정 없음 |

`rg` 기준 blast radius:

| 대상 | 직접 영향 | 위험도 |
|---|---|---|
| `QueueTicketMapper`, `QueueTicket_SQL_postgresql.xml` | 기존 대기번호 발급, 신규 대기열 조회/상태 전이 | MEDIUM |
| `QueueTicketVO` | 기존 체크인/현장접수 발급 응답 내부 매핑, 신규 Queue 응답 | LOW |
| 신규 `QueueController`, `QueueCommandService`, `QueueQueryService` | 신규 `/api/queues/**` API | LOW |

## 5. 구현 체크리스트

- [x] Controller 추가
- [x] QueryService 추가
- [x] CommandService 추가
- [x] Policy 추가
- [x] DTO 추가
- [x] Mapper 수정
- [x] Mapper XML 수정
- [x] Swagger 테스트용 seed/mock 데이터 추가
- [x] API 명세 갱신

## 6. 검증 체크리스트

- [x] Maven compile 확인
- [x] Maven test-compile 확인
- [x] `git diff --check` 확인
- [x] Swagger URL과 인증 방법 작성
- [x] Swagger 대표 예시와 테스트 순서 작성
- [x] GitNexus detect-changes 시도 및 실패 사유 기록
- [ ] 사용자가 Docker PostgreSQL 실행 확인
- [ ] 사용자가 VS Code Spring Boot Dashboard로 서버 기동 확인
- [ ] 사용자가 Swagger에서 대표 예시로 API 런타임 호출 확인

## 7. 구현 내용

### 7.1 API

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| GET | `/api/queues` | 오늘 대기열 조회 | 같은 보건소 `STAFF`, `ADMIN` |
| POST | `/api/queues/{queueTicketId}/call` | 대기자 호출 | 같은 보건소 `STAFF`, `ADMIN` |
| POST | `/api/queues/{queueTicketId}/start` | 처리 시작 | 같은 보건소 `STAFF`, `ADMIN` |
| POST | `/api/queues/{queueTicketId}/complete` | 처리 완료 | 같은 보건소 `STAFF`, `ADMIN` |

### 7.2 상태 전이

- `WAITING`, `HOLD` 상태만 `CALLED`로 호출할 수 있다.
- `CALLED` 상태만 `IN_PROGRESS`로 처리 시작할 수 있다.
- `IN_PROGRESS` 상태만 `COMPLETED`로 처리 완료할 수 있다.
- 처리 시작 시 Visit 상태도 `IN_PROGRESS`가 된다.
- 처리 완료 시 Visit 상태도 `COMPLETED`가 된다.
- 예약 기반 방문이면 예약 상태도 `COMPLETED`가 된다.

### 7.3 Swagger 테스트용 seed

- `data.sql`에 `Swagger대기열 / 010-5678-9012` 현장 방문과 `WAITING` 대기표 seed를 추가했다.
- 애플리케이션 재시작 시 해당 seed 방문/대기표를 지운 뒤 다시 생성해 상태 전이 테스트에 재사용한다.
- `GET /api/queues?status=WAITING`에서 `visitorName = Swagger대기열`의 `queueTicketId`를 확인한 뒤 호출/시작/완료에 사용한다.

## 8. 사용자 직접 런타임 확인 방법

### 8.1 Swagger 인증

1. `POST /api/auth/login`에서 `staff@test.com / password1234`로 로그인한다.
2. Swagger Authorize 창에는 `Bearer `를 제외한 accessToken 값만 입력한다.
3. curl 같은 직접 호출에서는 `Authorization: Bearer {accessToken}` 형식을 사용한다.

### 8.2 Swagger 대표 테스트 순서

1. `GET /api/queues?status=WAITING` 실행
2. `visitorName = Swagger대기열`의 `queueTicketId` 확인
3. `POST /api/queues/{queueTicketId}/call` 실행
4. `POST /api/queues/{queueTicketId}/start` 실행
5. `POST /api/queues/{queueTicketId}/complete` 실행

기대 결과:

- 호출 후 `status = CALLED`
- 처리 시작 후 `status = IN_PROGRESS`
- 처리 완료 후 `status = COMPLETED`

## 9. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 범위 정리 | 보류 `POST /api/queues/{id}/hold` 구현 | 대기 흐름도에는 HOLD가 있으나 이번 브랜치는 조회/호출/시작/완료에 집중 | 후속 작업 |
| 범위 정리 | 대기번호 발급 동시성 정책 보강 | 현재는 업무 유형별 당일 최대 번호 + 1 방식이라 동시 접수 경쟁 조건 검증 필요 | 후속 작업 |
| 범위 정리 | 방문/대기 취소 전용 API | 접수 이후 취소 정책이 필요함 | 후속 작업 |
| 검증 중 | GitNexus detect-changes 확인 | CLI에서 `unknown command 'detect-changes'`로 실패 | `git status`, `git diff --check`, Maven 검증으로 보완 |

## 10. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리
