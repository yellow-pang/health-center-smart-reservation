# Reservation Check-In API 구현 기록

## 1. 작업 목표

- 직원 또는 관리자가 예약번호로 예약자를 체크인할 수 있게 한다.
- 체크인 성공 시 예약 상태를 `CHECKED_IN`으로 바꾸고 Visit과 QueueTicket을 함께 생성한다.
- 체크인 이후 기존 예약 취소 API가 상태 정책으로 실패하도록 예약/방문/대기 경계를 정리한다.

## 2. 작업 범위

- [x] 이번 브랜치에 포함: `POST /api/visits/check-in`
- [x] 이번 브랜치에 포함: 예약 상태 `CHECKED_IN` 변경
- [x] 이번 브랜치에 포함: 예약 방문 `visits` 생성
- [x] 이번 브랜치에 포함: 대기번호 `queue_tickets` 생성
- [x] 이번 브랜치에 포함: 체크인 이후 예약 취소 불가 정책 정리
- [x] 이번 브랜치에서 제외: 현장 접수 `POST /api/visits/walk-in`
- [x] 이번 브랜치에서 제외: 대기열 조회, 호출, 처리 시작, 완료 API
- [x] 이번 브랜치에서 제외: 방문/대기 취소 전용 API

## 3. 작업 전 체크리스트

- [x] `docs/README.md` 확인
- [x] `docs/09_agent/05_문서기반_자동진행_운영가이드.md` 확인
- [x] `docs/11_implementation_log/00_브랜치_작업_기록_가이드.md` 확인
- [x] `docs/13_schedule/02_전체_작업_체크리스트.md` 관련 항목 확인
- [x] `docs/02_domain/02_업무_흐름도.md` 확인
- [x] `docs/03_database/01_ERD_및_테이블_명세서.md` 확인
- [x] `docs/04_api/01_API_명세서.md` 확인
- [x] 현재 브랜치와 작업 트리 확인
- [x] 영향받는 파일 확인

## 4. 영향 분석

GitNexus 확인:

| 대상 | 결과 | 보완 확인 |
|---|---|---|
| `gitnexus status` | stale index 확인 | `rg`와 Maven 검증 병행 |
| `gitnexus analyze .` | `.gitnexus/lbug.shadow` 접근 거부로 실패 | 기존 인덱스 impact 시도 후 실패 기록 |
| `gitnexus impact ReservationCommandService` | exit 1, 상세 출력 없음 | `rg` 직접 참조 확인 |
| `gitnexus impact ReservationMapper` | exit 1, 상세 출력 없음 | `rg` 직접 참조 확인 |
| `gitnexus impact SecurityConfig` | exit 1, 상세 출력 없음 | 기존 `/api/visits/**` 권한 규칙 확인 |

`rg` 기준 blast radius:

| 대상 | 직접 영향 | 위험도 |
|---|---|---|
| `ReservationMapper` | `ReservationCommandService`, `ReservationQueryService`, 신규 `VisitCommandService` | MEDIUM |
| `Reservation_SQL_postgresql.xml` | 예약 조회/생성/취소와 신규 체크인 조회/상태 변경 | MEDIUM |
| 신규 `VisitCommandService` | 신규 `VisitController`에서만 호출 | LOW |
| 신규 `VisitMapper`, `QueueTicketMapper` | 신규 체크인 흐름에서만 호출 | LOW |

## 5. 구현 체크리스트

- [x] Controller 추가
- [x] CommandService 추가
- [x] Policy 추가
- [x] Mapper 추가
- [x] Mapper XML 추가
- [x] DTO 추가
- [x] PostgreSQL schema에 `visits`, `queue_tickets` 추가
- [x] Swagger 체크인 스모크 테스트용 seed 예약 추가
- [x] 예외 처리 확인
- [x] 공통 응답 형식 확인

## 6. 검증 체크리스트

- [x] Maven compile 확인
- [x] Maven test-compile 확인
- [x] Swagger 우선 API 확인 방법과 대표 예시 데이터 작성
- [x] Swagger 확인 URL과 확인 항목 작성
- [x] `git diff --check` 확인
- [ ] `gitnexus detect-changes` 확인
- [ ] 사용자가 Docker PostgreSQL 실행 확인
- [ ] 사용자가 VS Code Spring Boot Dashboard로 서버 기동 확인
- [ ] 사용자가 Swagger에서 대표 예시로 API 런타임 호출 확인
- [ ] 사용자가 PR 문서의 추가 테스트 체크리스트 확인

## 7. 구현 내용

### 7.1 API

| Method | URL | 설명 | 권한 |
|---|---|---|---|
| POST | `/api/visits/check-in` | 예약자 체크인 및 대기번호 발급 | 같은 보건소 `STAFF`, `ADMIN` |

### 7.2 정책

- `RESERVED` 상태 예약만 체크인할 수 있다.
- 체크인 담당자는 예약과 같은 보건소 소속 `STAFF` 또는 `ADMIN`이어야 한다.
- 체크인, Visit 생성, QueueTicket 생성은 하나의 트랜잭션에서 처리한다.
- 체크인 이후 예약 상태는 `CHECKED_IN`이므로 기존 예약 취소 API는 `RESERVATION_CANCEL_INVALID_STATUS`로 실패한다.
- 대기번호는 MVP 기준으로 업무 유형별 당일 최대 번호 + 1로 발급한다.
- Swagger 테스트용 seed 예약번호는 `RSV-SWAGGER-CHECKIN-001`이다.
- 앱 재시작 시 seed 예약은 다시 `RESERVED` 상태로 초기화되어 체크인 스모크 테스트에 재사용할 수 있다.

## 8. 사용자 직접 런타임 확인 방법

이번 브랜치의 런타임 검증은 에이전트가 `mvn spring-boot:run`으로 서버를 직접 띄우지 않고, 사용자가 Docker PostgreSQL, VS Code Spring Boot Dashboard, Swagger UI로 확인한다. API 테스트는 터미널 명령보다 Swagger `Try it out`을 우선 사용한다.

### 8.1 Docker PostgreSQL 실행

```powershell
docker compose up -d postgres
docker compose ps
```

기대 결과:

- PostgreSQL 컨테이너가 `Up` 상태다.
- 백엔드가 사용할 DB 포트가 정상 노출되어 있다.

### 8.2 VS Code Spring Boot Dashboard 서버 기동

확인 방법:

- VS Code 왼쪽 Spring Boot Dashboard에서 backend 애플리케이션을 실행한다.
- 콘솔에서 Spring Boot started 로그를 확인한다.
- 기본 접속 주소는 `http://localhost:8080`이다.

기대 결과:

- 애플리케이션이 종료되지 않고 기동 상태를 유지한다.
- PostgreSQL schema 초기화가 적용되어 `visits`, `queue_tickets` 테이블을 사용할 수 있다.

### 8.3 Swagger 접속

```text
http://localhost:8080/swagger-ui/index.html
```

기대 결과:

- Swagger UI가 열리고 `AuthController`, `ReservationController`, `VisitController`가 보인다.

### 8.4 Swagger 인증

1. Swagger에서 `POST /api/auth/login`을 연다.
2. `Try it out`을 누른다.
3. 아래 대표 예시로 실행한다.

```json
{
  "email": "staff@test.com",
  "password": "password1234"
}
```

기대 결과:

- `success`가 `true`
- `data.accessToken`이 발급된다.
- Swagger 상단 또는 API별 Authorization 입력란에 `Bearer {accessToken}` 형식으로 입력한다.

### 8.5 Swagger 예약자 체크인

1. 체크인할 예약번호를 준비한다.
   - 기본 seed 예약번호 `RSV-SWAGGER-CHECKIN-001`을 사용한다.
   - 이미 체크인해서 중복 체크인 오류가 나면 백엔드를 재시작해 seed 예약을 다시 `RESERVED` 상태로 초기화한 뒤 재시도한다.
2. Swagger에서 `VisitController`의 `POST /api/visits/check-in`을 연다.
3. `Try it out`을 누르고 아래 대표 예시 1개로 실행한다.

```json
{
  "reservationNo": "RSV-SWAGGER-CHECKIN-001"
}
```

기대 결과:

- HTTP 201
- `success`가 `true`
- `data.visitId`가 생성됨
- `data.queueTicketId`가 생성됨
- `data.ticketNumber`가 1 이상의 숫자
- `data.status`가 `WAITING`

### 8.6 추가 확인은 PR 체크리스트에서 수행

대표 예시로 정상 체크인을 확인한 뒤, 체크인 이후 예약 취소 불가와 중복 체크인 같은 추가 케이스는 PR 문서의 테스트 체크리스트를 보고 사용자가 완료 여부를 체크한다.


## 9. Swagger 확인 항목

URL:

```text
http://localhost:8080/swagger-ui/index.html
```

확인:

- `VisitController`에 `POST /api/visits/check-in` 노출
- 요청 DTO `reservationNo`와 대표 예시 데이터 확인
- 성공 응답이 `success + data + error` 구조인지 확인
- 실패 응답 코드 `FORBIDDEN`, `RESERVATION_NOT_FOUND`, `ALREADY_CHECKED_IN` 확인

## 10. 진행 중 발견된 추가 작업

| 발견 시점 | 추가 작업 | 이유 | 처리 |
|---|---|---|---|
| 구현 중 | 현장 접수 API 구현 | Visit Context 완료 기준에 현장 접수가 포함됨 | 후속 작업 |
| 구현 중 | 대기열 조회/호출 API 구현 | 발급된 QueueTicket을 직원이 처리할 수 있어야 전체 흐름이 닫힘 | 후속 작업 |
| 구현 중 | 대기번호 발급 동시성 보강 | 현재는 업무 유형별 당일 최대 번호 + 1 방식이라 동시 체크인 경쟁 조건 검증 필요 | 후속 작업 |
| 구현 중 | 방문/대기 취소 전용 정책 정리 | 체크인 이후 예약 취소는 막았지만 방문/대기 자체 취소 API는 아직 없음 | 후속 작업 |
| 검증 중 | GitNexus detect-changes 재확인 | CLI가 상세 출력 없이 exit 1로 실패함 | 후속 확인 |
| 검증 중 | Swagger 체크인 seed 운영 방식 정리 | 체크인 성공 후 같은 예약번호는 재사용할 수 없어 앱 재시작 시 seed를 초기화하도록 함 | data.sql 반영 |

## 11. 브랜치 종료 전 체크리스트

- [x] 구현 기록 작성 완료
- [x] 전체 체크리스트 갱신
- [x] PR 문서 작성
- [x] 남은 위험 기록
- [x] 후속 작업 기록
- [x] 커밋 메시지 정리
